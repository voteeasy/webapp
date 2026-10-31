package de.voteeasy.gathering;

import de.voteeasy.participation.ParticipationService;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantRepository;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VoteProposal;
import de.voteeasy.votes.VoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatheringServiceUTest {
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private ParticipationService participationService;
    @Mock
    private VoteService voteService;
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private GatheringService gatheringService;

    private static Collection<GrantedAuthority> getTenantAdminAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
        return grantedAuthorities;
    }

    private static Collection<GrantedAuthority> getRoleAdminAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return grantedAuthorities;
    }

    @Test
    void ensureRetrievalOfGatheringsIsByTenant() {
        Tenant tenant = createUserWithTenant();

        Gathering gathering = new Gathering();
        gathering.setName("MyTestGathering");
        when(gatheringRepository.findByOrganisation(eq(tenant))).thenReturn(Sets.newSet(gathering));

        Set<Gathering> gatherings = gatheringService.getAllWithTenantCheck();
        assertThat(gatherings).hasSize(1).contains(gathering);
    }

    private Tenant createUserWithTenant() {
        User user = new User();
        user.setUserName("test");
        Tenant tenant = new Tenant("test");
        user.setOrganisation(tenant);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));
        return tenant;
    }

    @Test
    void saveAsGivenJustSavesWithoutAnyModifications() {
        Gathering g = new Gathering();

        gatheringService.saveAsGiven(g);
        verify(gatheringRepository).save(eq(g));
    }

    @Test
    void saveWithTenantAndUserAugmentsGatheringWithTenantAndUser() {
        User user = new User();
        user.setUserName("test");
        Tenant tenant = new Tenant("test");
        tenant = tenantRepository.save(tenant);
        user.setOrganisation(tenant);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        UUID gatheringId = UUID.randomUUID();
        Gathering g = new Gathering();
        g.setName("name");
        g.setStartedAt(0L);
        g.setId(gatheringId);
        gatheringService.saveWithTenantAndUser(g);

        Gathering augmentedGathering = new Gathering("name", tenant);
        augmentedGathering.setStartedAt(0L);
        augmentedGathering.setId(gatheringId);
        augmentedGathering.setStartedBy(user);

        verify(gatheringRepository).save(eq(augmentedGathering));
        verify(applicationUserService).currentlyLoggedInUser();
    }

    @Test
    void getWithTenantCheckUnknownId() {
        assertThat(gatheringService.getWithTenantCheck(UUID.randomUUID())).isEmpty();
        verifyNoInteractions(gatheringRepository);
    }

    @Test
    void getWithTenantCheckNoUserLoggedIn() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.empty());

        assertThat(gatheringService.getWithTenantCheck(UUID.randomUUID())).isEmpty();
        verifyNoInteractions(gatheringRepository);
    }

    @Test
    void getWithTenantCheckLoggedInUserHasNoTenant() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));

        assertThat(gatheringService.getWithTenantCheck(UUID.randomUUID())).isEmpty();
        verifyNoInteractions(gatheringRepository);
    }

    @Test
    void getWithTenantCheckTalksToRepositoryProperly() {
        createUserWithTenant();
        UUID id = UUID.randomUUID();
        Gathering g = new Gathering();
        when(gatheringRepository.findByIdAndOrganisation(eq(id), any())).thenReturn(Optional.of(g));

        assertThat(gatheringService.getWithTenantCheck(id)).isPresent().contains(g);
        verify(gatheringRepository).findByIdAndOrganisation(eq(id), any());
    }

    @Test
    void deleteCascadinglyNeedsSpecialTenantAdminRole() {
        when(gatheringRepository.findByOrganisation(any())).thenReturn(Sets.newSet(new Gathering()));
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Tenant t = new Tenant("deleteCascadinglyNeedsSpecialTenantAdminRole");
        gatheringService.deleteCascadinglyWithoutTenantCheck(t);
        // no deletion took place/call to removeGathering(Gathering)
        verifyNoInteractions(participationService);
    }

    @Test
    void deleteCascadinglyWithUnknownTenant() {
        when(gatheringRepository.findByOrganisation(any())).thenReturn(Collections.emptySet());
        gatheringService.deleteCascadinglyWithoutTenantCheck(new Tenant("deleteCascadinglyWithUnknownTenant"));
        // no deletion took place/call to removeGathering(Gathering)
        verifyNoInteractions(participationService);
    }

    @Test
    void deleteCascadinglyWithNullTenant() {
        gatheringService.deleteCascadinglyWithoutTenantCheck((Tenant) null);
        verifyNoInteractions(participationService);
    }

    @Test
    void deleteCascadinglyInteractsWithUnderlyingJPARepositoriesOrServices() {
        Gathering g = new Gathering();
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);
        g.setVoteProposals(Sets.newSet(VoteProposal.builder().id(UUID.randomUUID()).name("VP2").build(), VoteProposal.builder().id(UUID.randomUUID()).name("VP2").build()));
        when(gatheringRepository.findByOrganisation(any())).thenReturn(Sets.newSet(g));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // tricky mocking due to WildcardGenerics in return type of getAuthorities()
        doReturn(getTenantAdminAuthorities()).when(authentication).getAuthorities();

        Tenant t = new Tenant("deleteCascadinglyNeedsSpecialTenantAdminRole");
        gatheringService.deleteCascadinglyWithoutTenantCheck(t);

        // verify deletion took place recursively
        verify(participationService).deleteFor(eq(g));
        verify(voteService, times(2)).deleteProposalById(any());
        verify(gatheringRepository).deleteById(eq(gatheringId));
    }

    @Test
    void deleteCascadinglyByGatheringInteractsWithUnderlyingJPARepsitoryMethods() {
        Tenant t = createUserWithTenant();
        Gathering g = new Gathering();
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);
        g.setOrganisation(t);
        g.setVoteProposals(Sets.newSet(VoteProposal.builder().id(UUID.randomUUID()).name("VP2").build(), VoteProposal.builder().id(UUID.randomUUID()).name("VP2").build()));
        when(gatheringRepository.findByIdAndOrganisation(eq(gatheringId), any())).thenReturn(Optional.of(g));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // tricky mocking due to WildcardGenerics in return type of getAuthorities()
        doReturn(getRoleAdminAuthorities()).when(authentication).getAuthorities();

        gatheringService.deleteCascadinglyWithoutTenantCheck(gatheringId);

        // verify deletion took place recursively
        verify(participationService).deleteFor(eq(g));
        verify(voteService, times(2)).deleteProposalById(any());
        verify(gatheringRepository).deleteById(eq(gatheringId));
    }

    @Test
    void deleteCascadinglyByGatheringIdWithNullAsInput() {
        gatheringService.deleteCascadinglyWithoutTenantCheck((UUID) null);
        verifyNoInteractions(gatheringRepository);
    }

}
