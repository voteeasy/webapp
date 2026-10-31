package de.voteeasy.selfservice;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.votes.VotingController;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class SelfServiceRegistrationController {

    private final SelfServiceRegistrationService selfServiceRegistrationService;

    @Autowired
    public SelfServiceRegistrationController(SelfServiceRegistrationService selfServiceRegistrationService) {
        this.selfServiceRegistrationService = selfServiceRegistrationService;
    }

    @RequestMapping("/registerselfservice")
    public ModelAndView registerSelfService(Model model) {
        model.addAttribute("register", SelfServiceRegistrationForm.builder().build());
        return new ModelAndView("registerselfservice.html");
    }

    @PostMapping("/registerselfservice")
    public ModelAndView processRegisterForm(@Valid @ModelAttribute("register") final SelfServiceRegistrationForm form, final BindingResult result, final RedirectAttributes redirectAttributes) {
        if (result.hasErrors() || selfServiceRegistrationService.isCaptchaWrong(form.getCaptcha())) {
            if (selfServiceRegistrationService.isCaptchaWrong(form.getCaptcha())) {
                // FIXME #410: i18n is not properly shown, but i18n key instead.
                // filed as Thymeleaf issue https://github.com/thymeleaf/thymeleaf/issues/1029 and stackoverflow question
                result.addError(new FieldError("register", "captcha", "Zur Zustimmung bitte das STERN-Symbol auswählen. Please choose the STAR symbol to continue."));
            }
            return new ModelAndView("registerselfservice.html");
        }

        final ServiceResult serviceResult = selfServiceRegistrationService.createOrganisation(form);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());

        if (!serviceResult.getStatus().is2xxSuccessful()) {
            return new ModelAndView("redirect:/registerselfservice?error");
        }

        ModelAndView targetView = new ModelAndView("redirect:/registerselfservice?regsuccess");
        if (selfServiceRegistrationService.isSomeoneLoggedIn()) {
            targetView.addObject("logoutHint", true);
        }
        targetView.addObject("initpw", serviceResult.get(SelfServiceRegistrationService.INITIAL_PASSWORD));
        targetView.addObject("initapw", serviceResult.get(SelfServiceRegistrationService.INITIAL_ADMIN_PASSWORD));
        targetView.addObject("inittenant", serviceResult.get(SelfServiceRegistrationService.TENANT_NAME));
        targetView.addObject("inittime", serviceResult.get(SelfServiceRegistrationService.CREATION_TIMESTAMP));
        return targetView;
    }
}
