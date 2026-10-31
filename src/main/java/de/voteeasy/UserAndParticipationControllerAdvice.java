package de.voteeasy;

import de.voteeasy.participation.ParticipationService;
import de.voteeasy.user.ApplicationUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice
public class UserAndParticipationControllerAdvice {
    private final ApplicationUserService applicationUserService;
    private final ParticipationService participationService;
    private final HttpServletRequest servletRequest;

    @Autowired
    public UserAndParticipationControllerAdvice(ApplicationUserService applicationUserService, ParticipationService participationService, HttpServletRequest servletRequest) {
        this.applicationUserService = applicationUserService;
        this.participationService = participationService;
        this.servletRequest = servletRequest;
    }

    @ModelAttribute
    public void handleRequest(Model model) {
        participationService.getCurrentParticipation().ifPresent(participation -> model.addAttribute("participation", participation));
        applicationUserService.currentlyLoggedInUser().ifPresent(user -> model.addAttribute("user", user));
        if (servletRequest != null) {
            model.addAttribute("servletPath", servletRequest.getServletPath());
        }
    }

}
