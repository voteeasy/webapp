package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.*;

@Service
@Transactional
@Slf4j
@Validated
public class VotingService {

    private final VoteProposalRepository voteProposalRepository;
    private final OptionRepository optionRepository;
    private final GatheringService gatheringService;
    private final ApplicationUserService applicationUserService;

    @Autowired
    public VotingService(VoteProposalRepository voteProposalRepository, OptionRepository optionRepository, GatheringService gatheringService, ApplicationUserService applicationUserService) {
        this.voteProposalRepository = voteProposalRepository;
        this.optionRepository = optionRepository;
        this.gatheringService = gatheringService;
        this.applicationUserService = applicationUserService;
    }

    public Set<VoteProposal> getProposalsForGathering(@NotNull UUID gatheringId) {
        final Optional<User> loggedInUser = applicationUserService.currentlyLoggedInUser();
        if (loggedInUser.isEmpty()) {
            log.warn("Someone tried to retrieve data from gathering with id {} without being logged in.", gatheringId);
            return Collections.emptySet();
        }

        Optional<Gathering> g = gatheringService.getWithTenantCheck(gatheringId);
        if (g.isEmpty()) {
            log.info("Requested proposals for gathering with unknown id {} (possibly from different tenant) - returning empty result instead.", gatheringId);
            return Collections.emptySet();
        }

        if (!loggedInUser.get().getOrganisation().equals(g.get().getOrganisation())) {
            log.warn("A user {} tried to retrieve gathering from different target tenant {}", loggedInUser.get().getId(), g.get().getOrganisation());
            return Collections.emptySet();
        }

        return new TreeSet<>(g.get().getVoteProposals());
    }

    /*
     * Adds the proposal and persists all entities belonging to the new entity.
     */
    public ServiceResult addProposal(@NotNull @Valid VoteProposalForm voteProposalForm) {
        Optional<Gathering> g = gatheringService.getWithTenantCheck(voteProposalForm.getGatheringId());
        if (g.isEmpty()) {
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }
        log.info("Will add votingProposal to gathering with id {}", g.get().getId());

        final VoteProposal voteProposal = VoteProposalFormMapper.toEntity(voteProposalForm);
        voteProposal.setGathering(g.get());
        g.get().getVoteProposals().add(voteProposal);

        VoteProposal voteProposalDatabase = voteProposalRepository.save(voteProposal);
        log.info("Saved votingProposal with id {} to gathering with id {}", voteProposalDatabase.getId(), g.get().getId());
        voteProposal.getOptions().forEach(o -> o.setVoteProposal(voteProposalDatabase));
        optionRepository.saveAll(voteProposal.getOptions());
        log.info("Persisted {} options for new voting proposal", voteProposal.getOptions().size());

        return ServiceResult.builder().messageKey("success.proposal").status(HttpStatus.OK).build();
    }

}
