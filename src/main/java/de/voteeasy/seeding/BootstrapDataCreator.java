package de.voteeasy.seeding;

import de.voteeasy.gathering.GatheringRepository;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
public class BootstrapDataCreator {

    private final GatheringService gatheringService;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final VoteProposalRepository voteProposalRepository;
    private final OptionRepository optionRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    @Autowired
    public BootstrapDataCreator(GatheringService gatheringService, GatheringRepository gatheringRepository, UserRepository userRepository, VoteProposalRepository voteProposalRepository, OptionRepository optionRepository, RoleRepository roleRepository, TenantRepository tenantRepository) {
        this.gatheringService = gatheringService;
        this.gatheringRepository = gatheringRepository;
        this.userRepository = userRepository;
        this.voteProposalRepository = voteProposalRepository;
        this.optionRepository = optionRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
    }

    /*
     * As example data needs to be bootstrapped in the correct order we do this synchronously.
     */
    @Bean
    @Profile("!test")
    @Transactional
    public CommandLineRunner bootstrapExampleAndBaseData() {
        return (args) -> {
            new ProfileSeedDataCreator().bootstrapRoleData(roleRepository);
            new UserSeedDataCreator().bootstrapUserRelatedData(roleRepository, userRepository, tenantRepository);
            new GatheringSeedDataCreator().bootstrapGatheringData(gatheringRepository, gatheringService, userRepository, voteProposalRepository, optionRepository, tenantRepository);
            log.info("Finished bootstrap data loading.");
        };
    }
}

