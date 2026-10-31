package de.voteeasy.tenant;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
@PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
@RequestMapping("settings/tenant")
public class TenantController {
    private final ApplicationUserService applicationUserService;
    private final TenantService tenantService;
    private final TenantExportService tenantExportService;

    @Autowired
    public TenantController(ApplicationUserService applicationUserService, TenantService tenantService, TenantExportService tenantExportService) {
        this.applicationUserService = applicationUserService;
        this.tenantService = tenantService;
        this.tenantExportService = tenantExportService;
    }

    @GetMapping("/")
    public ModelAndView list(Model model) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN");
        if (user.isEmpty()) {
            log.error("SPOOFING ATTEMPT: Unable to list tenants without a given user.");
            return new ModelAndView("redirect:/");
        }

        model.addAttribute("tenants", tenantService.list());

        return new ModelAndView("/settings/tenant/index.html");
    }

    @GetMapping("/overdue")
    public ModelAndView listOverdue(Model model) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN");
        if (user.isEmpty()) {
            log.error("SPOOFING ATTEMPT: Unable to list overdue tenants without a given user.");
            return new ModelAndView("redirect:/");
        }

        model.addAttribute("tenants", tenantService.listOverdue());

        return new ModelAndView("/settings/tenant/overdue.html");
    }

    @GetMapping("/export")
    public void csvList(HttpServletResponse response) throws IOException {
        tenantExportService.export(response, tenantService.list());
    }

    @GetMapping("/add")
    public ModelAndView showCreateTenantForm(Model model) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("TENANT_ADMIN");
        if (user.isEmpty()) {
            log.error("SPOOFING ATTEMPT: Unable to create tenant without a given user.");
            return new ModelAndView("redirect:/");
        }

        model.addAttribute("tenant", CreateTenantForm.builder().build());
        return new ModelAndView("/settings/tenant/add.html");
    }

    @PostMapping("/add")
    public ModelAndView createTenant(@Valid @ModelAttribute("tenant") final CreateTenantForm form, final BindingResult result, final RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return new ModelAndView("/settings/tenant/add");
        } else {
            final ServiceResult serviceResult = tenantService.createTenant(form);
            if (HttpStatus.FORBIDDEN.equals(serviceResult.getStatus())) {
                result.addError(new FieldError("tenant", "name", serviceResult.getMessageKey()));
                return new ModelAndView("/settings/tenant/add");
            }

            log.info("Creating new tenant was {}", serviceResult.getStatus().name());
            redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, serviceResult.getMessageKey());
        }

        return new ModelAndView("redirect:/settings/tenant/");
    }

    @PostMapping("/toggle/{tenantId}")
    public ModelAndView toggleTenant(@PathVariable("tenantId") UUID tenantId, RedirectAttributes redirectAttributes) {
        ServiceResult result = tenantService.toggleTenant(tenantId);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());

        return new ModelAndView("redirect:/settings/tenant/");
    }

    @PostMapping("/delete/{tenantId}")
    public ModelAndView deleteTenant(@PathVariable("tenantId") UUID tenantId, RedirectAttributes redirectAttributes) {
        ServiceResult result = tenantService.deleteTenant(tenantId);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());
        return new ModelAndView("redirect:/settings/tenant/");
    }
}
