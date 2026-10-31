package de.voteeasy.selfservice;

import de.voteeasy.serviceresult.ServiceResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SelfServiceRegistrationControllerUTest {

    @Mock
    private SelfServiceRegistrationService service;

    @Mock
    private BindingResult result;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SelfServiceRegistrationController controller;

    @Test
    void postSubmitFormOkayWithUserLoggedIn() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder() //
                .captcha("testCaptcha").tenantname("testtenantworksfine")
                .build();
        Map<String, String> payload = new HashMap<>();
        payload.put(SelfServiceRegistrationService.INITIAL_PASSWORD, "testmypw");
        payload.put(SelfServiceRegistrationService.INITIAL_ADMIN_PASSWORD, "testapw");
        payload.put(SelfServiceRegistrationService.TENANT_NAME, "testtenantworksfine");
        payload.put(SelfServiceRegistrationService.CREATION_TIMESTAMP, "4711");

        ServiceResult serviceResult = ServiceResult.builder() //
                .messageKey("myi18nkey").status(HttpStatus.OK) //
                .payload(payload)
                .build();

        when(result.hasErrors()).thenReturn(false);
        doReturn(false).when(service).isCaptchaWrong(eq("testCaptcha"));

        when(service.createOrganisation(eq(form))).thenReturn(serviceResult);
        when(service.isSomeoneLoggedIn()).thenReturn(Boolean.TRUE);

        ModelAndView modelAndView = controller.processRegisterForm(form, result, redirectAttributes);
        assertThat(modelAndView.getViewName()).isEqualTo("redirect:/registerselfservice?regsuccess");

        // ensure all attributes are mapped completely
        assertThat(modelAndView.getModelMap().getAttribute("initpw")).isEqualTo("testmypw");
        assertThat(modelAndView.getModelMap().getAttribute("initapw")).isEqualTo("testapw");
        assertThat(modelAndView.getModelMap().getAttribute("inittenant")).isEqualTo("testtenantworksfine");
        assertThat(modelAndView.getModelMap().getAttribute("inittime")).isEqualTo("4711");
        assertThat(modelAndView.getModelMap().getAttribute("logoutHint")).isEqualTo(Boolean.TRUE);
    }

    @Test
    void postSubmitFormOkayWithNoUserLoggedIn() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder() //
                .captcha("testCaptcha").tenantname("testatenantworksfine")
                .build();
        Map<String, String> payload = new HashMap<>();
        payload.put(SelfServiceRegistrationService.TENANT_NAME, "testatenantworksfine");

        ServiceResult serviceResult = ServiceResult.builder() //
                .messageKey("myi18nkey").status(HttpStatus.OK) //
                .payload(payload)
                .build();

        when(result.hasErrors()).thenReturn(false);
        doReturn(false).when(service).isCaptchaWrong(eq("testCaptcha"));

        when(service.createOrganisation(eq(form))).thenReturn(serviceResult);
        when(service.isSomeoneLoggedIn()).thenReturn(Boolean.FALSE);

        ModelAndView modelAndView = controller.processRegisterForm(form, result, redirectAttributes);
        assertThat(modelAndView.getViewName()).isEqualTo("redirect:/registerselfservice?regsuccess");

        // ensure no logoutHint is shown
        assertThat(modelAndView.getModelMap().getAttribute("inittenant")).isEqualTo("testatenantworksfine");
        assertThat(modelAndView.getModelMap().getAttribute("logoutHint")).isNull();
    }

    @Test
    void postSubmitWithErrorsInService() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder() //
                .captcha("testCaptcha").tenantname("testbtenantworksfine")
                .build();

        ServiceResult serviceResult = ServiceResult.builder() //
                .status(HttpStatus.CONFLICT) //
                .build();

        when(result.hasErrors()).thenReturn(false);
        doReturn(false).when(service).isCaptchaWrong(eq("testCaptcha"));

        when(service.createOrganisation(eq(form))).thenReturn(serviceResult);

        ModelAndView modelAndView = controller.processRegisterForm(form, result, redirectAttributes);
        assertThat(modelAndView.getViewName()).isEqualTo("redirect:/registerselfservice?error");
        assertThat(modelAndView.getModelMap()).isEmpty();
    }

    @Test
    void postSubmitWithWrongCaptcha() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder() //
                .captcha("testCaptcha")
                .build();
        doReturn(true).when(service).isCaptchaWrong(eq("testCaptcha"));

        ModelAndView modelAndView = controller.processRegisterForm(form, result, redirectAttributes);
        assertThat(modelAndView.getViewName()).isEqualTo("registerselfservice.html");
        assertThat(modelAndView.getModelMap()).isEmpty();

        verify(result).addError(new FieldError("register", "captcha", "Zur Zustimmung bitte das STERN-Symbol auswählen. Please choose the STAR symbol to continue."));
    }

    @Test
    void postSubmitWithValidationErrorsAndCorrectCaptcha() {
        SelfServiceRegistrationForm form = SelfServiceRegistrationForm.builder() //
                .captcha("testCaptcha")
                .build();
        doReturn(false).when(service).isCaptchaWrong(eq("testCaptcha"));
        when(result.hasErrors()).thenReturn(true);

        ModelAndView modelAndView = controller.processRegisterForm(form, result, redirectAttributes);
        assertThat(modelAndView.getViewName()).isEqualTo("registerselfservice.html");
        assertThat(modelAndView.getModelMap()).isEmpty();
        verify(result).hasErrors();
        verifyNoMoreInteractions(result);
    }


}
