package de.voteeasy.settings.tenant;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingControllerUTest {
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private SettingService settingService;
    @Mock
    private Model model;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SettingController settingController;

    @Test
    void verifyInjection() {
        assertThat(settingController).isNotNull();
    }

    @Test
    void showAddSettingFormWithoutUser() {
        assertThat(settingController.showAddSettingForm(model, UUID.randomUUID()).getViewName()).isEqualTo("redirect:/");
    }

    @Test
    void showAddSettingFormWithoutValidTenant() {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN")).thenReturn(Optional.of(new User()));
        assertThat(settingController.showAddSettingForm(model, UUID.randomUUID()).getViewName()).isEqualTo("redirect:/");
    }

    @Test
    void showAddSettingFillsAllNeededFieldsProperly() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN")).thenReturn(Optional.of(user));

        Tenant tenant = new Tenant("showAddSettingFillsAllNeededFieldsProperly");
        tenant.setId(tenantId);
        when(settingService.getTenantDetails(eq(tenantId))).thenReturn(Optional.of(tenant));

        final Model realModel = new ExtendedModelMap();
        final ModelAndView result = settingController.showAddSettingForm(realModel, tenantId);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("tsettings/properties/property-add.html");

        assertThat(realModel.containsAttribute("setting")).isTrue();
        // make sure all parameters are propely put into the settingsForm
        assertThat(realModel.getAttribute("setting")).isInstanceOf(SettingPropertyForm.class);
        SettingPropertyForm form = (SettingPropertyForm) realModel.getAttribute("setting");
        assertThat(form).isNotNull();
        // user input fields are null
        assertThat(form.getKey()).isNull();
        assertThat(form.getScope()).isNull();
        assertThat(form.getType()).isNull();
        assertThat(form.getValue()).isNull();
        // and other values are extracted from currently logged in user
        assertThat(form.getTenantId()).isEqualTo(tenantId);
        assertThat(form.getTenantName()).isEqualTo("showAddSettingFillsAllNeededFieldsProperly");
        assertThat(form.getUserId()).isEqualTo(userId);
    }

    @Test
    void submitFormWithInvalidTenantId() {
        UUID id = UUID.randomUUID();
        when(settingService.getTenantDetails(any(UUID.class))).thenReturn(Optional.empty());
        final ModelAndView result = settingController.submitAddSettingForm(model, id, SettingPropertyForm.builder().build(), bindingResult, redirectAttributes);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("tsettings/properties/property-add.html");
    }

    @Test
    void submitFormWithFormValidationErrors() {
        UUID id = UUID.randomUUID();
        when(bindingResult.hasErrors()).thenReturn(true);
        when(settingService.getTenantDetails(any(UUID.class))).thenReturn(Optional.of(new Tenant("submitFormWithFormValidationErrors")));
        final ModelAndView result = settingController.submitAddSettingForm(model, id, SettingPropertyForm.builder().build(), bindingResult, redirectAttributes);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("tsettings/properties/property-add.html");
    }

    @Test
    void submitFormRedirectInSuccessCase() {
        UUID tenantId = UUID.randomUUID();
        Tenant t = new Tenant("showAddSettingFillsAllNeededFieldsProperly");
        SettingPropertyForm form = SettingPropertyForm.builder().build();

        when(settingService.getTenantDetails(eq(tenantId))).thenReturn(Optional.of(t));
        ServiceResult serviceResult = ServiceResult.builder().messageKey("myTestSucceeds").status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).build();
        when(settingService.addProperty(eq(form), eq(t))).thenReturn(serviceResult);

        final ModelAndView result = settingController.submitAddSettingForm(model, tenantId, form, bindingResult, redirectAttributes);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/settings/tenant/");
        verify(redirectAttributes).addFlashAttribute(eq(VotingController.FLASH_MESSAGE), eq("myTestSucceeds"));
    }

}
