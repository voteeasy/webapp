package de.voteeasy.gathering;

import de.voteeasy.participation.ParticipationService;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VoteProposal;
import de.voteeasy.votes.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
@Slf4j
@Validated
public class GatheringService {
    private final GatheringRepository gatheringRepository;
    private final VoteService voteService;
    private final ApplicationUserService applicationUserService;
    private final ParticipationService participationService;

    @Autowired
    public GatheringService(GatheringRepository gatheringRepository, VoteService voteService, ApplicationUserService applicationUserService, ParticipationService participationService) {
        this.gatheringRepository = gatheringRepository;
        this.voteService = voteService;
        this.applicationUserService = applicationUserService;
        this.participationService = participationService;
    }

    @Transactional
    public Gathering saveWithTenantAndUser(@Valid @NotNull Gathering gathering) {
        Optional<User> currentUser = applicationUserService.currentlyLoggedInUser();
        if (currentUser.isPresent()) {
            User u = currentUser.get();
            gathering.setStartedBy(u);
            gathering.setOrganisation(u.getOrganisation());
        }

        return gatheringRepository.save(gathering);
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Transactional
    public Gathering saveAsGiven(@Valid @NotNull Gathering g) {
        return gatheringRepository.save(g);
    }

    @Transactional(readOnly = true)
    public Set<Gathering> getAllWithTenantCheck() {
        Set<Gathering> gatherings = new TreeSet<>();

        Optional<User> currentUser = applicationUserService.currentlyLoggedInUser();
        currentUser.ifPresent(user -> gatherings.addAll(gatheringRepository.findByOrganisation(user.getOrganisation())));

        return gatherings;
    }

    @Transactional(readOnly = true)
    public Optional<Gathering> getWithTenantCheck(UUID id) {
        Optional<User> currentUser = applicationUserService.currentlyLoggedInUser();
        if (currentUser.isEmpty()) {
            log.warn("Someone tried to retrieve data with gatheringId {} without being logged in.", id);
            return Optional.empty();
        }

        Tenant currentTenant = currentUser.get().getOrganisation();
        if (currentTenant == null) {
            log.error("User {} tried to retrieve data without having a valid tenant. Data corruption.", currentUser);
            return Optional.empty();
        }

        return gatheringRepository.findByIdAndOrganisation(id, currentUser.get().getOrganisation());
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Transactional
    public void deleteCascadinglyWithoutTenantCheck(@NotNull Tenant tenant) {
        Set<Gathering> gatherings = gatheringRepository.findByOrganisation(tenant);
        if (!gatherings.isEmpty() && AuthorityUtils.authorityListToSet(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).contains("ROLE_TENANT_ADMIN")) {
            log.info("Removing {} gatherings", gatherings.size());
            gatherings.forEach(this::removeGathering);
        }
    }

    /*
     * Remove given gathering and all depending objects in the following order:
     * <li>Participation
     * <li>Vote per VoteProposal
     * <li>VoteProposal
     * <li>Gathering
     */
    private void removeGathering(@NotNull Gathering g) {
        log.warn("Will remove gathering {}", g.getId());
        // remove all participations
        participationService.deleteFor(g);

        // remove all VoteProposals and their votes respectively before deleting the gathering itself
        for (VoteProposal voteProposal : g.getVoteProposals()) {
            voteService.deleteProposalById(voteProposal.getId());
        }
        gatheringRepository.deleteById(g.getId());
        log.info("Successfully removed gathering with gatheringId.");
    }

    @Transactional
    public void deleteCascadinglyWithoutTenantCheck(@NotNull UUID gatheringId) {
        Optional<Gathering> g = getWithTenantCheck(gatheringId);
        if (g.isPresent() && AuthorityUtils.authorityListToSet(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).contains("ROLE_ADMIN")) {
            removeGathering(g.get());
        }
    }

}
