package de.voteeasy.invitation;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.votes.VotingController;
import de.voteeasy.user.UserRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@Slf4j
public class UserTokenRegistrationController {
    private final RegisterByUserTokenService registerByUserTokenService;

    @Autowired
    public UserTokenRegistrationController(RegisterByUserTokenService registerByUserTokenService) {
        this.registerByUserTokenService = registerByUserTokenService;
    }

    @RequestMapping("/register")
    public ModelAndView showRegisterForm(Model model) {
        model.addAttribute("register", RegisterByUserTokenForm.builder().build());
        return new ModelAndView("register.html");
    }

    @PostMapping("/register")
    public ModelAndView processRegisterForm(@Valid @ModelAttribute("register") final RegisterByUserTokenForm form, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return new ModelAndView("/register.html");
        }

        final ServiceResult serviceResult = registerByUserTokenService.redeemToken(form);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());

        if (!serviceResult.getStatus().is2xxSuccessful()) {
            return new ModelAndView("redirect:/register?error");
        }

        ModelAndView targetView = new ModelAndView("redirect:/register?regsuccess");
        targetView.addObject("newTenant", serviceResult.get(UserRegistrationService.KEY_TENANT));
        targetView.addObject("newUsername", serviceResult.get(UserRegistrationService.KEY_USERNAME));
        return targetView;
    }

}
