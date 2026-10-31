package de.voteeasy.seeding;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringRepository;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import de.voteeasy.votes.OptionRepository;
import de.voteeasy.votes.VoteProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatheringSeedDataCreatorTest {
    private static final int SEEDED_TENANT_COUNT = 3;

    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private GatheringService gatheringService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VoteProposalRepository voteProposalRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private TenantRepository tenantRepository;

    private GatheringSeedDataCreator gatheringSeedDataCreator;

    @BeforeEach
    void initSeedCreator() {
        gatheringSeedDataCreator = new GatheringSeedDataCreator();
    }

    private void triggerDataSeeding() {
        gatheringSeedDataCreator.bootstrapGatheringData(gatheringRepository, gatheringService, userRepository, voteProposalRepository, optionRepository, tenantRepository);
    }

    @Test
    @ExpectSystemExitWithStatus(-1)
    void bootstrapGatheringDataExitsApplicationIfTenantIsNotAlreadyInitialized() {
        when(tenantRepository.findByName(any())).thenReturn(Optional.empty());
        triggerDataSeeding();
    }

    @Test
    @ExpectSystemExitWithStatus(-1)
    void bootstrapGatheringDataExitsApplicationIfUserIsNotAlreadyInitialized() {
        when(tenantRepository.findByName(anyString())).thenReturn(Optional.of(new Tenant("Test")));
        triggerDataSeeding();
    }

    @Test
    void bootstrapGatheringDataSuccessCase() {
        simulatePreconditionsExist();
        triggerDataSeeding();
        // verify mock interactions when persisting seeding data
        verify(gatheringService, times(2 * SEEDED_TENANT_COUNT)).saveWithTenantAndUser(any());
        verify(voteProposalRepository, times(2 * SEEDED_TENANT_COUNT * 2)).save(any());
        verify(optionRepository, times(39)).save(any());

        // emulate all data is saved to the database
        when(gatheringRepository.findByOrganisation(any())).thenReturn(Sets.newSet(new Gathering()));
        triggerDataSeeding();
        verifyNoMoreInteractions(gatheringService);
        verifyNoMoreInteractions(voteProposalRepository);
        verifyNoMoreInteractions(optionRepository);
    }

    private void simulatePreconditionsExist() {
        for (String tenant : UserSeedDataCreator.TENANTS) {
            Tenant t = new Tenant(tenant);
            t.setId(UUID.randomUUID());
            when(tenantRepository.findByName(eq(tenant))).thenReturn(Optional.of(t));

            User gongbing = new User("gongbing", "Gongbing@" + tenant, "pwHash", t);
            gongbing.setId(UUID.randomUUID());
            lenient().when(userRepository.getByUserInTenant("gongbing", t)).thenReturn(Optional.of(gongbing));
        }

        Gathering g = new Gathering();
        g.setId(UUID.randomUUID());
        g.setName("JustAMeetingForTests");
        when(gatheringService.saveWithTenantAndUser(any())).thenReturn(g);
    }

}

