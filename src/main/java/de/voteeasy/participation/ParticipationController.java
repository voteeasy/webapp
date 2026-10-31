package de.voteeasy.participation;

import de.voteeasy.gathering.GatheringService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.votes.VotingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
@RequestMapping("participation")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class ParticipationController {
    private final ParticipationService participationService;
    private final GatheringService gatheringService;

    @Autowired
    public ParticipationController(ParticipationService participationService, GatheringService gatheringService) {
        this.participationService = participationService;
        this.gatheringService = gatheringService;
    }

    @RequestMapping("/")
    public ModelAndView showAllForUser() {
        ModelAndView modelAndView = new ModelAndView("participation/index");
        modelAndView.addObject("participations", participationService.getAllParticipations());

        return modelAndView;
    }

    @PostMapping("/participate/{gatheringId}")
    public RedirectView takePart(@PathVariable("gatheringId") UUID gatheringId, RedirectAttributes redirectAttributes) {
        ServiceResult result = participationService.participateIn(gatheringService.getWithTenantCheck(gatheringId));
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());
        if (HttpStatus.OK.equals(result.getStatus())) {
            return new RedirectView("/gatherings/participate/" + gatheringId);
        }

        return new RedirectView("/gatherings/");
    }

    @PostMapping("/leave/{gatheringId}")
    public RedirectView leave(@PathVariable("gatheringId") UUID gatheringId, RedirectAttributes redirectAttributes) {
        ServiceResult result = participationService.leave();
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());
        if (HttpStatus.OK.equals(result.getStatus())) {
            return new RedirectView("/gatherings/");
        }

        return new RedirectView("/gatherings/participate/" + gatheringId);
    }

    @RequestMapping("/members")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ModelAndView showMembersOfCurrentMeeting() {
        ModelAndView modelAndView = new ModelAndView("participation/participants");
        modelAndView.addObject("participants", participationService.getParticipants());

        return modelAndView;
    }

}
