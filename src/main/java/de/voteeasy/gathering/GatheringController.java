package de.voteeasy.gathering;

import de.voteeasy.participation.ParticipationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.validation.Valid;
import java.util.UUID;

@Controller
@RequestMapping("gatherings")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class GatheringController {
    private static final String HOME_LINK = "gatherings/index";

    private final GatheringService gatheringService;
    private final ParticipationService participationService;

    @Autowired
    public GatheringController(GatheringService gatheringService, ParticipationService participationService) {
        this.gatheringService = gatheringService;
        this.participationService = participationService;
    }

    @RequestMapping("/")
    public ModelAndView showAll() {
        ModelAndView modelAndView = new ModelAndView(HOME_LINK);
        participationService.getCurrentParticipation().ifPresent(participation -> modelAndView.addObject("participation", participation));
        modelAndView.addObject("gatherings", gatheringService.getAllWithTenantCheck());
        return modelAndView;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RedirectView deleteGathering(@PathVariable("id") UUID id) {
        gatheringService.deleteCascadinglyWithoutTenantCheck(id);
        return new RedirectView("/gatherings/");
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ModelAndView showCreationForm(Gathering gathering) {
        return new ModelAndView("gatherings/add");
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ModelAndView addGathering(@Valid Gathering gathering, BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("gatherings/add");
        }

        gatheringService.saveWithTenantAndUser(gathering);
        log.info("Saved new gathering.");
        return new ModelAndView("redirect:/gatherings/");
    }

    @GetMapping("/participate/{id}")
    public ModelAndView joinGathering(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("gathering", gatheringService.getWithTenantCheck(id));

        return new ModelAndView("gatherings/participate");
    }

}
