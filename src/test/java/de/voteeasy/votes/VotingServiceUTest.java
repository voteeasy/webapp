package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingServiceUTest {
    @Mock
    private VoteProposalRepository voteProposalRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private GatheringService gatheringService;
    @Mock
    private ApplicationUserService applicationUserService;

    @InjectMocks
    private VotingService votingService;

    @Test
    void wiringWorks() {
        assertThat(votingService).isNotNull();
    }

    @Test
    void retrieveGatheringFromDifferentTenantIsNotPossible() {
        Tenant hacker = new Tenant("evilHacker");
        hacker.setId(UUID.randomUUID());
        User evilUser = new User("evilHacker", "fullyEvilHacker", "invalidHash", hacker);

        Tenant myOrg = new Tenant("myOrg");
        myOrg.setId(UUID.randomUUID());
        Gathering g = new Gathering("myOrgOnly", myOrg);
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);

        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(evilUser));
        when(gatheringService.getWithTenantCheck(eq(gatheringId))).thenReturn(Optional.of(g));

        assertThat(votingService.getProposalsForGathering(gatheringId)).isEmpty();
    }

    @Test
    void retrieveGatheringTakesTenantOfCurrentUserIntoAccount() {
        Tenant hacker = new Tenant("evilHacker");
        hacker.setId(UUID.randomUUID());
        User evilUser = new User("evilHacker", "fullyEvilHacker", "invalidHash", hacker);

        Gathering g = new Gathering("forHackersOnly", hacker);
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);

        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(evilUser));
        when(gatheringService.getWithTenantCheck(eq(gatheringId))).thenReturn(Optional.of(g));

        assertThat(votingService.getProposalsForGathering(gatheringId)).isNotNull().contains();
    }

    @Test
    void addProposalSuccessfulFlow() {
        Tenant myOrg = new Tenant("myOrg");
        myOrg.setId(UUID.randomUUID());

        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering("gName", myOrg);
        gathering.setId(gatheringId);
        gathering.setVoteProposals(Sets.newTreeSet());

        VoteProposal voteProposal = VoteProposal.builder().id(UUID.randomUUID()).build();

        when(gatheringService.getWithTenantCheck(gatheringId)).thenReturn(Optional.of(gathering));
        when(voteProposalRepository.save(any())).thenReturn(voteProposal);

        VoteProposalForm voteProposalForm = VoteProposalForm.builder().gatheringId(gatheringId).gatheringName(gathering.getName()).name("MyVoteProposal").build();
        final ServiceResult result = votingService.addProposal(voteProposalForm);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessageKey()).isEqualTo("success.proposal");

        verify(gatheringService).getWithTenantCheck(eq(gatheringId));
        verify(voteProposalRepository).save(any());
    }

}
