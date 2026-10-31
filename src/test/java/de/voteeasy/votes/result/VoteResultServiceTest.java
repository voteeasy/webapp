package de.voteeasy.votes.result;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteResultServiceTest {
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VoteProposalRepository voteProposalRepository;
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private GatheringService gatheringService;

    @InjectMocks
    private VoteResultService voteResultService;

    @Test
    void aggregateResultHasDefaultValues() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));
        when(gatheringService.getWithTenantCheck(any(UUID.class))).thenReturn(Optional.of(new Gathering()));
        final Optional<VoteResultView> result = voteResultService.aggregateResult(UUID.randomUUID());
        assertThat(result).isPresent();
        assertThat(result.get().isEmpty()).isTrue();
    }

    @Test
    void aggregateResultWithoutValidGatheringIdIsEmpty() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));
        final Optional<VoteResultView> result = voteResultService.aggregateResult(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    void aggregateResultWithoutLoggedInUser() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.empty());
        final Optional<VoteResultView> result = voteResultService.aggregateResult(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    void aggregateResultReadsGatheringInformationProperly() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));

        Gathering g = new Gathering("aggregateResultReadsGatheringInformationProperly", new Tenant("aggregateResultReadsGatheringInformationProperly"));
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);
        when(gatheringService.getWithTenantCheck(eq(gatheringId))).thenReturn(Optional.of(g));

        final Optional<VoteResultView> view = voteResultService.aggregateResult(gatheringId);
        assertThat(view).isPresent();
        assertThat(view.get().isEmpty()).isTrue();
        assertThat(view.get().getGathering().getId()).isEqualTo(gatheringId);
        assertThat(view.get().getGathering().getName()).isEqualTo("aggregateResultReadsGatheringInformationProperly");
        assertThat(view.get().getGeneratedAt()).isNotNull();
    }

    @Test
    void aggregateResultReadsVoteProposalInformationProperly() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));

        Gathering g = new Gathering("aggregateResultReadsVoteProposalInformationProperly", new Tenant("aggregateResultReadsVoteProposalInformationProperly"));
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);
        when(gatheringService.getWithTenantCheck(eq(gatheringId))).thenReturn(Optional.of(g));

        Set<Option> meetingOptions = new HashSet<>();
        meetingOptions.add(new Option("A"));
        meetingOptions.add(new Option("B"));
        meetingOptions.add(new Option("C"));
        VoteProposal meeting = VoteProposal.builder().itemOfAgenda("1").name("meeting").startedAt(1L).stoppedAt(123L).options(meetingOptions).build();

        when(voteRepository.findByProposal(eq(meeting))).thenReturn(generateVotes(meeting));

        Set<VoteProposal> voteProposals = new HashSet<>();
        voteProposals.add(meeting);
        when(voteProposalRepository.findByGatheringAndStartedAtNotNullAndStoppedAtNotNullOrderByStoppedAt(eq(g))).thenReturn(voteProposals);

        final Optional<VoteResultView> view = voteResultService.aggregateResult(gatheringId);
        assertThat(view).isPresent();
        assertThat(view.get().isEmpty()).isFalse();
        assertThat(view.get().getGeneratedAt()).isNotNull();
        assertThat(view.get().getVoteProposals()).hasSize(1);

        final VoteProposalResult proposalResult = view.get().getVoteProposals().get(0);
        assertThat(proposalResult.getItemOfAgenda()).isEqualTo("1");
        assertThat(proposalResult.getName()).isEqualTo("meeting");
        assertThat(proposalResult.getStartedAt()).isEqualTo(1L);
        assertThat(proposalResult.getStoppedAt()).isEqualTo(123L);

        assertThat(proposalResult.getOptions()).hasSize(3);
        assertThat(proposalResult.getOptions()).containsKeys("A", "B", "C");
        assertThat(proposalResult.getOptions().get("A").get()).isEqualTo(5L);
        assertThat(proposalResult.getOptions().get("B").get()).isEqualTo(2L);
        assertThat(proposalResult.getOptions().get("C").get()).isEqualTo(0L);
    }

    private Set<Vote> generateVotes(VoteProposal voteProposal) {
        Set<Vote> votes = new HashSet<>();
        votes.add(asVote(voteProposal, "B"));
        votes.add(asVote(voteProposal, "A"));
        votes.add(asVote(voteProposal, "A"));
        votes.add(asVote(voteProposal, "A"));
        votes.add(asVote(voteProposal, "A"));
        votes.add(asVote(voteProposal, "A"));
        votes.add(asVote(voteProposal, "B"));
        return votes;
    }

    private Vote asVote(VoteProposal voteProposal, String chosenOption) {
        Vote vote = new Vote();
        vote.setProposal(voteProposal);
        vote.setChosenOptions(Set.of(new Option(chosenOption, voteProposal)));
        vote.setId(UUID.randomUUID());

        return vote;
    }

    @Test
    void aggregateProperlyWithoutAnyOptions() {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));

        Gathering g = new Gathering("aggregateProperlyWithoutAnyOptions", new Tenant("aggregateProperlyWithoutAnyOptions"));
        UUID gatheringId = UUID.randomUUID();
        g.setId(gatheringId);
        when(gatheringService.getWithTenantCheck(eq(gatheringId))).thenReturn(Optional.of(g));

        VoteProposal meeting = VoteProposal.builder().itemOfAgenda("1").name("meeting").startedAt(1L).stoppedAt(123L).build();

        Set<VoteProposal> voteProposals = new HashSet<>();
        voteProposals.add(meeting);
        when(voteProposalRepository.findByGatheringAndStartedAtNotNullAndStoppedAtNotNullOrderByStoppedAt(eq(g))).thenReturn(voteProposals);

        final Optional<VoteResultView> view = voteResultService.aggregateResult(gatheringId);
        assertThat(view).isPresent();
        assertThat(view.get().isEmpty()).isFalse();
        assertThat(view.get().getVoteProposals()).hasSize(1);
    }
}
