package de.voteeasy.votes.result;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Controller
@RequestMapping("results")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class VoteResultController {
    private final VoteResultService voteResultService;

    @Autowired
    public VoteResultController(VoteResultService voteResultService) {
        this.voteResultService = voteResultService;
    }

    @GetMapping("/{gatheringId}")
    public ModelAndView showResultsForGathering(@NotNull @PathVariable UUID gatheringId, ModelMap model) {
        model.addAttribute("voteResult", voteResultService.aggregateResult(gatheringId));
        return new ModelAndView("/results/index.html", model);
    }
}
