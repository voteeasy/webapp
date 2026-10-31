package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.participation.Participation;
import de.voteeasy.participation.ParticipationService;
import de.voteeasy.serviceresult.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("votings")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class VotingController {
    public static final String FLASH_MESSAGE = "flashmessage";
    /**
     * TODO #38: extract as enum and use enum in controllers to set type of message - <a href="https://getbootstrap.com/docs/4.6/components/alerts/">...</a>
     * One of: dark, warning, success etc.
     */
    static final String FLASH_TYPE = "flashtype";
    private final GatheringService gatheringService;
    private final VotingService votingService;
    private final VoteService voteService;
    private final ParticipationService participationService;

    @Autowired
    public VotingController(GatheringService gatheringService, VotingService votingService, VoteService voteService, ParticipationService participationService) {
        this.gatheringService = gatheringService;
        this.votingService = votingService;
        this.voteService = voteService;
        this.participationService = participationService;
    }

    @GetMapping("/proposals/{id}")
    public ModelAndView joinGathering(@PathVariable("id") UUID id, ModelMap model) {
        model.addAttribute("proposals", votingService.getProposalsForGathering(id));
        model.addAttribute("gatheringId", id);
        return new ModelAndView("votings/proposals", model);
    }

    @GetMapping(value = "/proposals/add/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ModelAndView addNewVoteProposal(@PathVariable(name = "id") @NonNull UUID gatheringId, ModelMap model, RedirectAttributes redirectAttributes) {
        Optional<Gathering> g = gatheringService.getWithTenantCheck(gatheringId);
        if (g.isEmpty()) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "error.unknownId");
            log.warn("Illegal gatheringId '{}' used to create new voting proposal", gatheringId);
            return new ModelAndView("redirect:/gatherings/");
        }

        Gathering gathering = g.get();
        final VoteProposalForm form = VoteProposalForm.builder().gatheringId(gathering.getId()).gatheringName(gathering.getName()).build();
        model.addAttribute("gatheringId", form.getGatheringId());
        model.addAttribute("gatheringName", form.getGatheringName());
        model.addAttribute("proposal", form);

        return new ModelAndView("votings/proposal-add", model);
    }

    @PostMapping(value = "/proposals/addproposal")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ModelAndView saveVotingProposal(final @Valid @ModelAttribute("proposal") VoteProposalForm form, final BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Optional<Gathering> g = gatheringService.getWithTenantCheck(form.getGatheringId());
            if (g.isEmpty()) {
                redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "error.unknownId");
                log.error("Unable to retrieve gathering when trying to add a new VoteProposal, will redirect to event page. Related gathering data was id: {}, name: {}", form.getGatheringId(), form.getGatheringName());
                return new ModelAndView("redirect:/gatherings/");
            }

            model.addAttribute("gatheringId", form.getGatheringId());
            model.addAttribute("gatheringName", form.getGatheringName());
            return new ModelAndView("votings/proposal-add");
        }

        final ServiceResult persistenceResult = votingService.addProposal(form);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, persistenceResult.getMessageKey());
        log.debug("Add vote form result was {}", persistenceResult.getStatus());

        return new ModelAndView("redirect:/gatherings/participate/" + form.getGatheringId());
    }

    @PostMapping("/proposals/delete/{proposalId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RedirectView deleteProposal(@PathVariable UUID proposalId, Model model, RedirectAttributes redirectAttributes) {
        final Optional<VoteProposal> proposal = voteService.getVoteProposalByIdWithTenantCheck(proposalId);
        if (proposal.isEmpty()) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "error.unknownId");
            log.error("Someone tried to delete an unknown voting proposal by id {}", proposalId);
        } else {
            voteService.deleteProposalById(proposalId);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "success.proposalDeletion");
        }
        return new RedirectView("/gatherings/");
    }

    @GetMapping("/proposals/startVoting/{proposalId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RedirectView startVoting(@PathVariable UUID proposalId, RedirectAttributes redirectAttributes) {
        voteService.startVoting(proposalId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "success.votingStarted");

        Optional<VoteProposal> proposal = voteService.getVoteProposalByIdWithTenantCheck(proposalId);
        return proposal.map(voteProposal -> new RedirectView("/votings/proposals/" + proposal.get().getGathering().getId())).orElse(new RedirectView("/gatherings/"));
    }

    @GetMapping("/proposals/endVoting/{proposalId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RedirectView endVoting(@PathVariable UUID proposalId, RedirectAttributes redirectAttributes) {
        voteService.stopVoting(proposalId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "success.votingFinished");

        Optional<VoteProposal> proposal = voteService.getVoteProposalByIdWithTenantCheck(proposalId);
        return proposal.map(voteProposal -> new RedirectView("/votings/proposals/" + voteProposal.getGathering().getId())).orElse(new RedirectView("/gatherings/"));
    }

    @GetMapping("/vote/{proposalId}")
    public ModelAndView vote(@PathVariable UUID proposalId, ModelMap model, RedirectAttributes redirectAttributes) {
        Optional<VoteProposal> voteProposal = voteService.getVoteProposalByIdWithTenantCheck(proposalId);
        if (voteProposal.isEmpty()) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "error.unknownId");
            log.warn("Illegal proposalId '{}' used to create new vote", proposalId);
            return new ModelAndView("redirect:/gatherings/");
        }

        final Optional<Participation> participation = participationService.getCurrentParticipation();
        if (participation.isEmpty() || !participation.get().getEvent().equals(voteProposal.get().getGathering())) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "votes.need.participation");
            return new ModelAndView("redirect:/gatherings/");
        }

        final VoteForm form = VoteForm.builder().availableOptions(VoteFormMapper.extractAvailableOptions(voteProposal.get().getOptions())).voteProposalId(voteProposal.get().getId()).build();
        model.addAttribute("proposal", voteProposal.get());
        model.addAttribute("vote", form);

        return new ModelAndView("votings/vote", model);
    }

    // TODO #24: Using ModelAndView yields strange error
    @PostMapping(value = "/vote/dovote")
    public String doVote(@Valid @ModelAttribute("vote") final VoteForm vote, final BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Optional<VoteProposal> voteProposal = voteService.getVoteProposalByIdWithTenantCheck(vote.getVoteProposalId());
        if (result.hasErrors()) {
            // we need to add these attributes as they are not present within above form itself
            if (voteProposal.isPresent()) {
                VoteProposal proposal = voteProposal.get();
                model.addAttribute("proposal", proposal);
                vote.setVoteProposalId(proposal.getId());
                vote.setAvailableOptions(VoteFormMapper.extractAvailableOptions(proposal.getOptions()));
            }
            return "votings/vote";
        }

        // prevent URL mingling or manual vote attempt; a valid vote must happen in the currently participated meeting
        final Optional<Participation> participation = participationService.getCurrentParticipation();
        if (participation.isEmpty() || voteProposal.isEmpty() || !participation.get().getEvent().equals(voteProposal.get().getGathering())) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "votes.need.participation");
            return "redirect:/gatherings/";
        }

        ServiceResult serviceResult = voteService.performVote(vote);
        HttpStatus status = serviceResult.getStatus();
        if (!StringUtils.isEmpty(serviceResult.getMessageKey())) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, serviceResult.getMessageKey());
        }

        // prevent double voting
        if (HttpStatus.LOCKED.equals(status)) {
            Optional<Vote> proposal = voteService.getVoteFromUser(vote.getVoteProposalId());
            proposal.ifPresent(alreadyVotedVote -> model.addAttribute("vote", alreadyVotedVote));
            return "votes/my-vote";
        }

        if (!redirectAttributes.getFlashAttributes().containsKey(FLASH_MESSAGE)) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "error.notVoted");
        }

        Optional<VoteProposal> proposal = voteService.getVoteProposalByIdWithTenantCheck(vote.getVoteProposalId());
        return proposal.map(voteProposalVotedFor -> "redirect:/votings/proposals/" + proposal.get().getGathering().getId()).orElse("redirect:/gatherings/");
    }

}
