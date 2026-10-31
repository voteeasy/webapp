package de.voteeasy.admin;

import de.voteeasy.gathering.GatheringRepository;
import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.participation.ParticipationRepository;
import de.voteeasy.settings.tenant.SettingRepository;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposalRepository;
import de.voteeasy.votes.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminService {
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VoteProposalRepository voteProposalRepository;
    private final VoteRepository voteRepository;
    private final TenantRepository tenantRepository;
    private final OptionRepository optionRepository;
    private final ParticipationRepository participationRepository;
    private final UserTokenRepository userTokenRepository;
    private final SettingRepository settingRepository;

    @Autowired
    public AdminService(GatheringRepository gatheringRepository, UserRepository userRepository, RoleRepository roleRepository, VoteProposalRepository voteProposalRepository, VoteRepository voteRepository, TenantRepository tenantRepository, OptionRepository optionRepository, ParticipationRepository participationRepository, UserTokenRepository userTokenRepository, SettingRepository settingRepository) {
        this.gatheringRepository = gatheringRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.voteProposalRepository = voteProposalRepository;
        this.voteRepository = voteRepository;
        this.tenantRepository = tenantRepository;
        this.optionRepository = optionRepository;
        this.participationRepository = participationRepository;
        this.userTokenRepository = userTokenRepository;
        this.settingRepository = settingRepository;
    }

    public CardinalityResult calculateCardinalities() {
        CardinalityResult result = new CardinalityResult();
        result.put("Gathering", gatheringRepository.count());
        result.put("User", userRepository.count());
        result.put("Role", roleRepository.count());
        result.put("VoteProposal", voteProposalRepository.count());
        result.put("Vote", voteRepository.count());
        result.put("Tenant", tenantRepository.count());
        result.put("Option", optionRepository.count());
        result.put("Participation", participationRepository.count());
        result.put("PersistentLogins", userRepository.countPersistentLogins());
        result.put("UserToken", userTokenRepository.count());
        result.put("Setting", settingRepository.count());
        return result;
    }

    private Optional<Tenant> getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId);
    }

    public CardinalityResult calculateCardinalities(UUID tenantId) {
        CardinalityResult result = new CardinalityResult();

        Optional<Tenant> tenant = getTenantById(tenantId);
        if (tenant.isPresent()) {
            result.put("Tenant", 1L); // constant as we are querying for the current organisation
            result.put("User", userRepository.countAllByOrganisation(tenant.get()));
            result.put("UserToken", userTokenRepository.countAllByOrganisation(tenant.get()));
            result.put("Gathering", gatheringRepository.countAllByOrganisation(tenant.get()));
            result.put("Setting", settingRepository.countAllByTenant(tenant.get()));
            result.put("VoteProposal", voteProposalRepository.countAllByGathering_Organisation(tenant.get()));
            result.put("Vote", voteRepository.countAllByVoter_Organisation(tenant.get()));
            result.put("Option", optionRepository.countAllByVoteProposal_Gathering_Organisation(tenant.get()));
            result.put("Participation", participationRepository.countAllByEvent_Organisation(tenant.get()));
            result.put("PersistentLogins", userRepository.countPersistentLoginsByTenant(tenant.get().getId()));
        }
        return result;
    }
}
