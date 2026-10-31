package de.voteeasy.admin;

import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("app")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class LoginController {
    private final ApplicationUserService applicationUserService;

    @Autowired
    public LoginController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @GetMapping("/flashmessage")
    public ModelAndView testFlashMessageTemplate(Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("flashmessage", "headers.commands");
        redirectAttributes.addFlashAttribute("flashmessage", "headers.commands");

        return new ModelAndView("/app/index.html", model.asMap());
    }

    @RequestMapping("/username")
    public @ResponseBody
    String getUserName(@AuthenticationPrincipal String userName, Principal p) {
        log.info("Just for debugging purposes: User {}", userName);
        log.info("Just for debugging purposes: Principal {}", p);

        final Optional<User> currentlyLoggedInUser = applicationUserService.currentlyLoggedInUser();
        // for testing only - if application is configured completely as principal is injected via spring-security
        return currentlyLoggedInUser.map(user -> user.getUserName() + "/" + user.getClass()).orElse("ANONYMOUS");
    }

    @GetMapping("/errormessage/{status}")
    public ResponseEntity<Object> handleCall(@PathVariable("status") int status) {
        return switch (status) {
            case 500 -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            case 404 -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            case 403 -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            default -> ResponseEntity.status(HttpStatus.OK).body("ok");
        };
    }
}
