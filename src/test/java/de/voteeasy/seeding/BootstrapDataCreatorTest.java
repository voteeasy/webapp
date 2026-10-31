package de.voteeasy.seeding;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import de.voteeasy.gathering.GatheringRepository;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.RoleRepository;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BootstrapDataCreatorTest {
    @Mock
    private GatheringService gatheringService;
    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VoteProposalRepository voteProposalRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TenantRepository tenantRepository;

    @Test
    @ExpectSystemExitWithStatus(-1)
    void bootstrapExampleAndBaseData() throws Exception {
        CommandLineRunner clr = new BootstrapDataCreator(gatheringService, gatheringRepository, userRepository, voteProposalRepository, optionRepository, roleRepository, tenantRepository).bootstrapExampleAndBaseData();
        assertThat(clr).isNotNull();
        clr.run("");
    }
}