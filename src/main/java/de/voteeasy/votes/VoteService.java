package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class VoteService {

    private final VoteProposalRepository voteProposalRepository;
    private final VoteRepository voteRepository;
    private final OptionRepository optionRepository;
    private final ApplicationUserService applicationUserService;

    public VoteService(VoteProposalRepository voteProposalRepository, VoteRepository voteRepository, OptionRepository optionRepository, ApplicationUserService applicationUserService) {
        this.voteProposalRepository = voteProposalRepository;
        this.voteRepository = voteRepository;
        this.optionRepository = optionRepository;
        this.applicationUserService = applicationUserService;
    }

    @Transactional(readOnly = true)
    public Optional<VoteProposal> getVoteProposalByIdWithTenantCheck(@NotNull UUID voteProposalId) {
        Optional<User> currentUser = applicationUserService.currentlyLoggedInUser();
        if (currentUser.isEmpty()) {
            log.warn("Someone tried to retrieve VoteProposal with id {} without being logged in.", voteProposalId);
            return Optional.empty();
        }

        Optional<VoteProposal> voteProposal = voteProposalRepository.findById(voteProposalId);
        if (voteProposal.isPresent()) {
            Gathering g = voteProposal.get().getGathering();
            if (g != null && g.getOrganisation().equals(currentUser.get().getOrganisation())) {
                return voteProposal;
            }
        }

        return voteProposal;
    }

    @Transactional
    public void deleteProposalById(@NotNull UUID voteProposalId) {
        Optional<VoteProposal> proposal = getVoteProposalByIdWithTenantCheck(voteProposalId);
        if (proposal.isPresent()) {
            // delete all votes before removing votingProposal itself
            log.info("Successfully removed {} votes of proposal with id {}", voteRepository.deleteAllByProposal(proposal.get()), voteProposalId);

            voteProposalRepository.deleteById(voteProposalId);
            log.info("Successfully removed vote proposal with id {}", voteProposalId);
        }
    }

    @Transactional
    public void startVoting(@NotNull UUID voteProposalId) {
        Optional<VoteProposal> voteProposal = getVoteProposalByIdWithTenantCheck(voteProposalId);
        if (voteProposal.isPresent()) {
            VoteProposal proposal = voteProposal.get();
            if (proposal.getStartedAt() == null) {
                proposal.setStartedAt(System.currentTimeMillis());
                voteProposalRepository.save(proposal);
                log.info("Admin started voting on proposal with id {}", voteProposalId);
            }
        }
    }

    @Transactional
    public void stopVoting(@NotNull UUID voteProposalId) {
        Optional<VoteProposal> voteProposal = getVoteProposalByIdWithTenantCheck(voteProposalId);
        if (voteProposal.isPresent()) {
            VoteProposal proposal = voteProposal.get();
            if (proposal.getStartedAt() != null && proposal.getStoppedAt() == null) {
                proposal.setStoppedAt(System.currentTimeMillis());
                voteProposalRepository.save(proposal);
                log.info("Admin stopped voting on proposal with id {}", voteProposalId);
            }
        }
    }

    @Transactional
    public ServiceResult performVote(@Valid @NotNull VoteForm voteForm) {
        Optional<VoteProposal> voteProposal = voteProposalRepository.findById(voteForm.getVoteProposalId());

        if (voteProposal.isEmpty()) {
            log.info("Someone tried to vote for an unknown proposal with id {}", voteForm.getVoteProposalId());
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        final Long stoppedAt = voteProposal.get().getStoppedAt();
        if (stoppedAt != null && System.currentTimeMillis() >= stoppedAt) {
            log.info("Disallowed voting after vote has already been stopped for proposal id {}", voteForm.getVoteProposalId());
            return ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("error.closedAlready").build();
        }

        Vote vote = new Vote();
        vote.setProposal(voteProposal.get());

        if (voteForm.getSelectedOptions() != null) {
            vote.setChosenOptions(
                    optionRepository.findByNameInAndVoteProposalEquals(voteForm.getSelectedOptions(), voteProposal.get(), Sort.by("name"))
            );
        } else {
            log.error("#116: Someone tried to sneak in an invalid vote without any selected options.");
            return ServiceResult.builder().status(HttpStatus.BAD_REQUEST).messageKey("NotEmpty.option.options").build();
        }

        Optional<User> applicationUser = applicationUserService.currentlyLoggedInUser();
        if (applicationUser.isPresent()) {
            User u = applicationUser.get();

            // make sure that no admin can vote on disallowed proposals
            if (!voteProposal.get().isAdminAllowedToVote() && applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("ADMIN").isPresent()) {
                return ServiceResult.builder().status(HttpStatus.METHOD_NOT_ALLOWED).messageKey("proposal.symbol.noadminvote").build();
            }

            if (!isUserAllowedToVote(vote.getProposal().getId(), u)) {
                log.info("User {} already voted and is not allowed to vote multiple times in tenant {}.", u.getId(), u.getOrganisation().getId());
                return ServiceResult.builder().status(HttpStatus.LOCKED).messageKey("error.noDoubleVotingAllowed").build();
            }

            vote.setVoter(applicationUser.get());
        } else {
            log.error("Currently logged in user could not be found during voting! Form data was {}", voteForm);
            return ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("error.unknownId").build();
        }

        if (vote.getChosenOptions().isEmpty() || vote.getChosenOptions().size() != voteForm.getSelectedOptions().size()) {
            log.error("#116: We lost chosen vote options for tenant {} and voter {}", applicationUser.get().getOrganisation().getId(), applicationUser.get().getId());
            log.error("#116: chosenOptionsEmpty?: {}, #chosenOptions: {}, #selectedOptionsInForm: {}", vote.getChosenOptions().isEmpty(), vote.getChosenOptions().size(), voteForm.getSelectedOptions().size());
            return ServiceResult.builder().status(HttpStatus.BAD_REQUEST).messageKey("error.notVoted").build();
        }

        Vote savedVote = voteRepository.save(vote);
        log.info("Successfully saved vote with id {}", savedVote.getId());
        return ServiceResult.builder().status(HttpStatus.OK).messageKey("success.voted").build();
    }

    @Transactional(readOnly = true)
    public boolean isUserAllowedToVote(@NotNull UUID voteProposalId, @NotNull User u) {
        final Optional<VoteProposal> proposal = getVoteProposalByIdWithTenantCheck(voteProposalId);
        return proposal.filter(voteProposal -> voteRepository.findByVoterAndProposal(u, voteProposal).isEmpty()).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<Vote> getVoteFromUser(@NotNull UUID proposalId) {

        Optional<User> applicationUser = applicationUserService.currentlyLoggedInUser();
        if (applicationUser.isPresent()) {
            final Optional<VoteProposal> proposal = getVoteProposalByIdWithTenantCheck(proposalId);

            if (proposal.isPresent()) {
                return voteRepository.findByVoterAndProposal(applicationUser.get(), proposal.get());
            }
        }

        log.error("Currently logged in user could not be found during vote retrieval for proposal id {}!", proposalId);
        return Optional.empty();
    }

    @Transactional
    public ServiceResult deleteVote(@NotNull UUID voteId) {
        final Optional<Vote> vote = voteRepository.findById(voteId);
        if (vote.isEmpty()) {
            log.warn("Somebody tried to delete an unknown vote with id {}", voteId);
            return ServiceResult.builder().messageKey("vote.deletion.unknownId").status(HttpStatus.NOT_FOUND).build();
        }

        if (canUserDeleteOwnVote(vote.get())) {
            voteRepository.deleteById(voteId);
            log.info("Successfully removed vote with id {}", voteId);
            return ServiceResult.builder().messageKey("vote.deletion.success").status(HttpStatus.OK).build();
        }
        log.info("Vote deletion is not allowed for vote with id {}", voteId);
        return ServiceResult.builder().messageKey("vote.deletion.forbidden").status(HttpStatus.FORBIDDEN).build();
    }

    @Transactional(readOnly = true)
    boolean canUserDeleteOwnVote(@NotNull Vote vote) {
        if (vote == null || vote.getProposal() == null || vote.getVoter() == null) {
            return false;
        }

        return vote.getProposal().getStoppedAt() == null && applicationUserService.currentlyLoggedInUser().equals(Optional.ofNullable(vote.getVoter()));
    }

    public boolean shouldVotesBeShown(@NotNull Optional<VoteProposal> proposal) {
        if (proposal.isEmpty()) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        return proposal.get().getStoppedAt() != null;
    }
}
