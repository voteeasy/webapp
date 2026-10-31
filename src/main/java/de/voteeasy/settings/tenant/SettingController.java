package de.voteeasy.settings.tenant;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("tsettings/properties")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class SettingController {
    private final ApplicationUserService applicationUserService;
    private final SettingService settingService;

    @Autowired
    public SettingController(ApplicationUserService applicationUserService, SettingService settingService) {
        this.applicationUserService = applicationUserService;
        this.settingService = settingService;
    }

    @GetMapping("/")
    public ModelAndView showMyProperties(Model model) {
        model.addAttribute("properties", settingService.getMySettings());
        return new ModelAndView("tsettings/properties/myproperties.html");
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @GetMapping("/{tenantId}")
    public ModelAndView showPropertiesByTenantId(@PathVariable("tenantId") UUID id, Model model) {
        model.addAttribute("properties", settingService.getSettings(id));
        return new ModelAndView("tsettings/properties/properties.html");
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @GetMapping("/keys")
    public ModelAndView showAvailableKeys(Model model) {
        model.addAttribute("keys", settingService.getAllKeys(SettingScope.TENANT));
        return new ModelAndView("tsettings/properties/keys.html");
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @GetMapping("/add/{tenantId}")
    public ModelAndView showAddSettingForm(Model model, @PathVariable("tenantId") UUID id) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN");
        final Optional<Tenant> tenant = settingService.getTenantDetails(id);
        if (user.isEmpty() || tenant.isEmpty()) {
            return new ModelAndView("redirect:/");
        }

        Tenant t = tenant.get();
        SettingPropertyForm form = SettingPropertyForm.builder().userId(user.get().getId()).tenantId(t.getId()).tenantName(t.getName()).build();
        model.addAttribute("setting", form);
        return new ModelAndView("tsettings/properties/property-add.html");
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @PostMapping("/add/{targetTenantId}")
    public ModelAndView submitAddSettingForm(final Model model, @PathVariable("targetTenantId") final UUID id, @Valid @ModelAttribute("setting") final SettingPropertyForm form, final BindingResult result, RedirectAttributes redirectAttributes) {
        final Optional<Tenant> tenant = settingService.getTenantDetails(id);
        if (result.hasErrors() || tenant.isEmpty()) {
            return new ModelAndView("tsettings/properties/property-add.html");
        } else {
            final ServiceResult serviceResult = settingService.addProperty(form, tenant.get());
            log.info("Adding settings property for tenant {} was {}", tenant.get().getId(), serviceResult.getStatus().name());
            redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());
        }
        return new ModelAndView("redirect:/settings/tenant/");
    }

    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @PostMapping("/delete/{tenantId}/{settingId}")
    public ModelAndView deleteSetting(@PathVariable("tenantId") UUID tenantId, @PathVariable("settingId") UUID id, RedirectAttributes redirectAttributes) {
        final ServiceResult serviceResult = settingService.deleteProperty(id);
        log.info("Deleting settings property for tenant {} was {}", tenantId, serviceResult.getStatus().name());
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());

        return new ModelAndView("redirect:/tsettings/properties/" + tenantId);
    }
}
