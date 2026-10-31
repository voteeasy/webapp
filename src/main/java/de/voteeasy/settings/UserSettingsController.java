package de.voteeasy.settings;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.user.UserService;
import de.voteeasy.votes.VotingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;

@Controller
@Slf4j
@PreAuthorize("isAuthenticated()")
@RequestMapping("settings")
public class UserSettingsController {
    private final ApplicationUserService applicationUserService;
    private final UserService userService;

    @Autowired
    public UserSettingsController(ApplicationUserService applicationUserService, UserService userService) {
        this.applicationUserService = applicationUserService;
        this.userService = userService;
    }

    @GetMapping("/changeName")
    public ModelAndView showChangeNameForm(Model model) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty()) {
            log.error("SPOOFING ATTEMPT: Unable to edit user without a given user.");
            return new ModelAndView("redirect:/");
        }

        User u = user.get();
        final ChangeNameForm form = ChangeNameForm.builder().userId(u.getId()).fullName(u.getFullName()).userName(u.getUserName()).build();
        model.addAttribute("account", form);

        return new ModelAndView("/settings/changeName.html");
    }

    @PostMapping("/changeName")
    public ModelAndView changeName(@Valid @ModelAttribute("account") final ChangeNameForm form, final BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return new ModelAndView("/settings/changeName");
        } else {
            final ServiceResult serviceResult = userService.updateUserWithSecurityCheck(form);
            log.info("Changing display name was {}", serviceResult.getStatus().name());
            redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());
        }
        return new ModelAndView("redirect:/");
    }

    @GetMapping("/changePassword")
    public ModelAndView changePassword(Model model) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUser();
        if (user.isEmpty()) {
            log.error("SPOOFING ATTEMPT: Unable to change password without a given user.");
            return new ModelAndView("redirect:/");
        }

        User u = user.get();
        final ChangePasswordForm form = ChangePasswordForm.builder().userId(u.getId()).fullName(u.getFullName()).userName(u.getUserName().trim()).build();
        model.addAttribute("account", form);

        return new ModelAndView("/settings/changePassword.html");
    }

    @PostMapping("/changePassword")
    public ModelAndView changePassword(@Valid @ModelAttribute("account") final ChangePasswordForm form, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return new ModelAndView("/settings/changePassword");
        } else {
            final ServiceResult serviceResult = userService.updateUserWithSecurityCheck(form);
            log.info("Changing password for user was {}", serviceResult.getStatus().name());
            redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());
        }
        return new ModelAndView("redirect:/");
    }

}
