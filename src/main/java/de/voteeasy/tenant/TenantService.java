package de.voteeasy.tenant;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.invitation.UserToken;
import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.seeding.UserSeedDataCreator;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.settings.tenant.SettingRepository;
import de.voteeasy.user.Role;
import de.voteeasy.user.RoleService;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.Option;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposal;
import de.voteeasy.votes.VoteProposalRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

@Service
@Slf4j
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
@Transactional
@Validated
public class TenantService {
    /**
     * Days until a tenant is overdue for deletion. Currently set to {@code 60} days.
     */
    static final Long OVERDUE_PERIOD =  60 * 86400000L;
    private static final List<String> PROTECTED_TENANTS = List.of(UserSeedDataCreator.VOTE_EASY);
    private static final String PW_PREFIX = "{bcrypt}";
    private static final String ADMIN_SUFFIX = "-admin";
    private final TenantRepository tenantRepository;
    private final GatheringService gatheringService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final OptionRepository optionRepository;
    private final VoteProposalRepository voteProposalRepository;
    private final UserTokenRepository userTokenRepository;
    private final SettingRepository settingRepository;

    @Autowired
    public TenantService(TenantRepository tenantRepository, GatheringService gatheringService, UserRepository userRepository, RoleService roleService, OptionRepository optionRepository, VoteProposalRepository voteProposalRepository, UserTokenRepository userTokenRepository, SettingRepository settingRepository) {
        this.tenantRepository = tenantRepository;
        this.gatheringService = gatheringService;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.optionRepository = optionRepository;
        this.voteProposalRepository = voteProposalRepository;
        this.userTokenRepository = userTokenRepository;
        this.settingRepository = settingRepository;
    }

    @Transactional(readOnly = true)
    public Set<Tenant> list() {
        return new TreeSet<>(tenantRepository.findAll(Sort.by("name")));
    }

    @Transactional(readOnly = true)
    public Set<Tenant> listOverdue() {
        return new TreeSet<>(tenantRepository.findByCreatedAtIsBeforeOrderByCreatedAtAsc(System.currentTimeMillis() - OVERDUE_PERIOD));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Transactional
    public ServiceResult createTenant(@Valid @NotNull CreateTenantForm form) {

        // 1. verify name is still available
        final Set<Tenant> tenants = tenantRepository.findByNameIgnoreCase(form.getName());
        if (!tenants.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.tenants.doublenames").build();
        }

        // 2. create tenant
        Tenant newTenant = tenantRepository.save(new Tenant(form.getName()));
        String tenantName = form.getName();

        // 3. create admin user/ if a NPE happens here the whole app is corrupted
        Set<Role> adminRoles = Set.of(roleService.getByNameWithoutPrefix("ADMIN").get(), roleService.getByNameWithoutPrefix("USER").get());
        User adminUser = User.builder()
                .userName(tenantName + ADMIN_SUFFIX)
                .fullName(tenantName.toLowerCase())
                .organisation(newTenant).passwordHash(PW_PREFIX + new BCryptPasswordEncoder().encode(form.getAdminPassword()))
                .roles(adminRoles)
                .build();
        User adminInDb = userRepository.save(adminUser);
        log.info("Created new admin user {} in fresh tenant {}", adminInDb.getId(), newTenant.getId());

        // 4. create example gathering with voteProposals
        Gathering exampleMeeting = gatheringService.saveAsGiven(new Gathering("Beispielversammlung", newTenant, adminInDb));
        VoteProposal voteProposal = VoteProposal.builder()
                .itemOfAgenda("Top 1")
                .name("Wer wird Vorsitzender des Vereins?")
                .gathering(exampleMeeting)
                .isAdminAllowedToVote(false)
                .isAnonymized(true)
                .build();
        voteProposalRepository.save(voteProposal);
        createOptionsForVoteProposal(List.of("Person 1", "Person 2", "Person 3"), voteProposal);

        VoteProposal decision = VoteProposal.builder()
                .itemOfAgenda("Top 2")
                .createdAt(System.currentTimeMillis() + 120L)
                .name("Soll eine Rasenbewässerung angeschafft werden?")
                .gathering(exampleMeeting)
                .build();
        voteProposalRepository.save(decision);
        createOptionsForVoteProposal(List.of("Ja", "Nein", "Enthaltung"), decision);
        log.info("Created seed gathering/voteProposal data in new tenant {}", newTenant.getId());

        return ServiceResult.builder().status(HttpStatus.OK).messageKey("success.tenant.new").build();
    }

    private void createOptionsForVoteProposal(List<String> options, VoteProposal proposal) {
        for (String name : options) {
            optionRepository.save(new Option(name, proposal));
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ServiceResult deleteTenant(@NotNull UUID tenantId) {
        Optional<Tenant> tenantToDelete = getTenant(tenantId);
        if (tenantToDelete.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        Tenant tenant = tenantToDelete.get();
        if (isProtected(tenant)) {
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.tenant.protected").build();
        }

        log.warn("AUDIT: Starting to remove tenant {}", tenantId);

        // remove all userTokens
        final Set<UserToken> tenantTokens = userTokenRepository.findAllByOrganisation(tenant, Sort.by(Sort.Direction.ASC, "createdAt"));
        userTokenRepository.deleteAll(tenantTokens);
        log.info("Removed {} invitation tokens for tenant.", tenantTokens.size());

        // remove all settings
        log.info("Removed {} settings for tenant.", settingRepository.deleteAllByTenant(tenant));

        // remove gatherings of this tenant
        gatheringService.deleteCascadinglyWithoutTenantCheck(tenant);
        // gatherings are owned by users thus they need to go first
        log.info("Removed {} users for tenant {}", userRepository.deleteAllByOrganisation(tenant), tenant.getId());
        tenantRepository.delete(tenant);

        log.warn("AUDIT: Successfully finished to remove tenant {}.", tenantId);
        return ServiceResult.builder().status(HttpStatus.OK).messageKey("success.tenant.remove").build();
    }

    @Transactional
    public ServiceResult toggleTenant(@NotNull UUID tenantId) {
        Optional<Tenant> t = getTenant(tenantId);
        if (t.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        Tenant tenant = t.get();
        if (isProtected(tenant)) {
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.tenant.protected").build();
        }
        tenant.setEnabled(!tenant.isEnabled());
        tenantRepository.save(tenant);
        log.info("Toggled tenant {} to {}", tenant.getId(), tenant.isEnabled());

        return ServiceResult.builder().status(HttpStatus.OK).messageKey("success.tenant." + (tenant.isEnabled() ? "on" : "off")).build();
    }

    @PreAuthorize("isAuthenticated()")
    public boolean isProtected(@NotNull Tenant tenant) {
        if (tenant != null && !StringUtils.isEmpty(tenant.getName())) {
            for (String protectedTenant : PROTECTED_TENANTS) {
                if (protectedTenant.equalsIgnoreCase(tenant.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public Optional<Tenant> getTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId);
    }
}
