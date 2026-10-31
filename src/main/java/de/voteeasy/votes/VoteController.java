package de.voteeasy.votes;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Controller
@RequestMapping("votes")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class VoteController {

    private final VoteRepository voteRepository;
    private final VoteService voteService;
    private final VoteAggregatorService voteAggregatorService;
    private final ApplicationUserService applicationUserService;

    @Autowired
    public VoteController(VoteRepository voteRepository, VoteService voteService, VoteAggregatorService voteAggregatorService, ApplicationUserService applicationUserService) {
        this.voteRepository = voteRepository;
        this.voteService = voteService;
        this.voteAggregatorService = voteAggregatorService;
        this.applicationUserService = applicationUserService;
    }

    @RequestMapping("/")
    public ModelAndView showAllByTenantAndUser() {
        Set<Vote> myVotes = new TreeSet<>();

        Optional<User> userFromDatabase = applicationUserService.currentlyLoggedInUser();
        if (userFromDatabase.isPresent()) {
            myVotes = voteRepository.findByVoter(userFromDatabase.get());
        }

        ModelAndView modelAndView = new ModelAndView("votes/show");
        modelAndView.addObject("votes", myVotes);

        return modelAndView;
    }

    @GetMapping("/proposal/{id}")
    public ModelAndView showVotesForVoteProposal(@NotNull @PathVariable UUID id) {
        Set<Vote> myVotes = new TreeSet<>();

        ModelAndView modelAndView = new ModelAndView("votes/show-by-id");

        Optional<VoteProposal> proposal = voteService.getVoteProposalByIdWithTenantCheck(id);
        if (proposal.isPresent() && voteService.shouldVotesBeShown(proposal)) {
            VoteProposal voteProposal = proposal.get();

            myVotes = voteRepository.findByProposal(voteProposal);
            if (proposal.get().isAnonymized()) {
                log.debug("Removing user information from anonymized views.");
                myVotes.forEach(v -> v.setVoter(null));
            }

            modelAndView.addObject("proposal", proposal.get());
        }

        modelAndView.addObject("votes", myVotes);
        modelAndView.addObject("voteChartMap", voteAggregatorService.getAsChart(myVotes));

        return modelAndView;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ModelAndView deleteVote(@NotNull @PathVariable("id") UUID voteId, RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView("redirect:/votes/");

        final ServiceResult serviceResult = voteService.deleteVote(voteId);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());

        // TODO handle different cases properly
        return switch (serviceResult.getStatus()) {
            // return to individual vote page as vote cannot be deleted anymore
            default ->
                // return to above default page with correct flashMessage
                    modelAndView;
        };

    }

}
