package de.voteeasy.votes.result;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class VoteResultService {
    private final VoteRepository voteRepository;
    private final VoteProposalRepository voteProposalRepository;
    private final GatheringService gatheringService;
    private final ApplicationUserService applicationUserService;


    @Autowired
    public VoteResultService(VoteRepository voteRepository, VoteProposalRepository voteProposalRepository, GatheringService gatheringService, ApplicationUserService applicationUserService) {
        this.voteRepository = voteRepository;
        this.voteProposalRepository = voteProposalRepository;
        this.gatheringService = gatheringService;
        this.applicationUserService = applicationUserService;
    }

    public Optional<VoteResultView> aggregateResult(@NonNull UUID gatheringId) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty()) {
            return Optional.empty();
        }

        final Optional<Gathering> gathering = gatheringService.getWithTenantCheck(gatheringId);
        if (gathering.isEmpty()) {
            return Optional.empty();
        }

        final Set<VoteProposal> currentVoteProposals = voteProposalRepository.findByGatheringAndStartedAtNotNullAndStoppedAtNotNullOrderByStoppedAt(gathering.get());
        VoteResultView.VoteResultViewBuilder resultBuilder = VoteResultView.builder();
        resultBuilder.gathering(RelatedGathering.builder().id(gathering.get().getId()).name(gathering.get().getName()).build());

        List<VoteProposalResult> individualResults = new ArrayList<>();
        for (VoteProposal voteProposal : currentVoteProposals) {
            VoteProposalResult.VoteProposalResultBuilder proposalResultBuilder = VoteProposalResult.builder();
            proposalResultBuilder.itemOfAgenda(voteProposal.getItemOfAgenda());
            proposalResultBuilder.name(voteProposal.getName());
            proposalResultBuilder.startedAt(voteProposal.getStartedAt());
            proposalResultBuilder.stoppedAt(voteProposal.getStoppedAt());
            proposalResultBuilder.options(aggregateOptions(voteProposal));
            individualResults.add(proposalResultBuilder.build());
        }
        resultBuilder.voteProposals(individualResults);

        return Optional.of(resultBuilder.build());
    }

    private Map<String, AtomicLong> aggregateOptions(@NonNull VoteProposal voteProposal) {
        if (voteProposal.getOptions().isEmpty()) {
            return Collections.emptyMap();
        }

        // init all available options to zero
        Set<Option> options = voteProposal.getOptions();
        Map<String, AtomicLong> results = new HashMap<>(); // TreeMap would sort by name not by value first
        for (Option o : options) {
            results.put(o.getName(), new AtomicLong(0));
        }

        // count vote options incrementally
        final Set<Vote> votes = voteRepository.findByProposal(voteProposal);
        for (Vote v : votes) {
            for (Option chosenOption : v.getChosenOptions()) {
                // TODO #48 evaluate replacing by LongAdder/LongAdder.sum() as we only add one item per iteration
                results.computeIfPresent(chosenOption.getName(), (key, value) -> new AtomicLong(value.incrementAndGet()));
            }
        }
        return results;
    }
}
