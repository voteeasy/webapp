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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @InjectMocks
    private AdminService adminService;

    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private VoteProposalRepository voteProposalRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private SettingRepository settingRepository;

    @Test
    void wiringWorks() {
        assertThat(adminService).isNotNull();
    }

    @Test
    void cardinalityCalculationHappensForAllRepositoriesWiredIn() {
        CardinalityResult result = adminService.calculateCardinalities();
        assertThat(result).isNotNull();
        verify(gatheringRepository).count();
        verify(userRepository).count();
        verify(userRepository).countPersistentLogins();
        verify(roleRepository).count();
        verify(voteProposalRepository).count();
        verify(voteRepository).count();
        verify(tenantRepository).count();
        verify(optionRepository).count();
        verify(participationRepository).count();
        verify(userTokenRepository).count();
        verify(settingRepository).count();
    }

    @Test
    void cardinalityCalculationHappensForAllRepositoriesWiredInForATentant() {
        UUID tenantId = UUID.randomUUID();

        Tenant tenant = new Tenant("cardinalityCalculationHappensForAllRepositoriesWiredInForATentant");
        tenant.setId(tenantId);
        when(tenantRepository.findById(eq(tenantId))).thenReturn(Optional.of(tenant));

        CardinalityResult result = adminService.calculateCardinalities(tenantId);
        assertThat(result).isNotNull();
        verify(tenantRepository).findById(eq(tenantId)); // no explicit counting
        verify(userRepository).countAllByOrganisation(eq(tenant));
        verify(userTokenRepository).countAllByOrganisation(eq(tenant));
        verify(gatheringRepository).countAllByOrganisation(eq(tenant));
        verify(settingRepository).countAllByTenant(eq(tenant));
        verify(voteProposalRepository).countAllByGathering_Organisation(eq(tenant));
        verify(voteRepository).countAllByVoter_Organisation(eq(tenant));
        verify(optionRepository).countAllByVoteProposal_Gathering_Organisation(eq(tenant));
        verify(participationRepository).countAllByEvent_Organisation(eq(tenant));
        verify(userRepository).countPersistentLoginsByTenant(eq(tenantId));
    }
}
