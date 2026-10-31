package de.voteeasy.invitation;

import de.voteeasy.export.UserTokenCSVService;
import de.voteeasy.export.UserTokenExcelService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("settings/invitation")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j
public class UserTokenController {
    private final UserTokenService userTokenService;
    private final ApplicationUserService applicationUserService;
    private final UserTokenCSVService userTokenCSVService;
    private final UserTokenExcelService userTokenExcelService;
    private final TenantService tenantService;

    @Autowired
    public UserTokenController(UserTokenService userTokenService, ApplicationUserService applicationUserService, UserTokenCSVService userTokenCSVService, UserTokenExcelService userTokenExcelService, TenantService tenantService) {
        this.userTokenService = userTokenService;
        this.applicationUserService = applicationUserService;
        this.userTokenCSVService = userTokenCSVService;
        this.userTokenExcelService = userTokenExcelService;
        this.tenantService = tenantService;
    }

    @RequestMapping("/")
    public ModelAndView list(Model model, RedirectAttributes redirectAttributes) {
        final Optional<User> user = applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles("ADMIN");
        if (user.isEmpty()) {
            log.error("SPOOFING ATTEMPT: Unable to list tenants without a given user.");
            return new ModelAndView("redirect:/");
        }

        // one time generation if tenant is not protected
        if (!tenantService.isProtected(user.get().getOrganisation()) && userTokenService.getToken().isEmpty()) {
            ServiceResult result = userTokenService.createToken();
            redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());

            return new ModelAndView("redirect:/settings/invitation/");
        }

        model.addAttribute("tokens", userTokenService.getToken());
        model.addAttribute("redeemedCount", userTokenService.getRedeemedTokenCount());
        model.addAttribute("openCount", userTokenService.getOpenTokenCount());
        model.addAttribute("disabledCount", userTokenService.getDisabledTokenCount());

        return new ModelAndView("settings/invitation/index.html");
    }

    @GetMapping("/export")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        userTokenCSVService.export(response, userTokenService.getUsableToken());
    }

    @GetMapping("/exportXLS")
    public ResponseEntity<InputStreamResource> exportToXLS() throws IOException {
        return userTokenExcelService.export(userTokenService.getUsableToken());
    }

    @PostMapping("/toggle/{tokenId}")
    public ModelAndView toggleToken(@PathVariable("tokenId") UUID tokenId, RedirectAttributes redirectAttributes) {
        ServiceResult result = userTokenService.toggleToken(tokenId);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());

        return new ModelAndView("redirect:/settings/invitation/");
    }

    @PostMapping("/delete/{tokenId}")
    public ModelAndView deleteToken(@PathVariable("tokenId") UUID tokenId, RedirectAttributes redirectAttributes) {
        ServiceResult result = userTokenService.deleteToken(tokenId);
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());

        return new ModelAndView("redirect:/settings/invitation/");
    }

    @GetMapping("/create")
    public ModelAndView createTokens(RedirectAttributes redirectAttributes) {
        ServiceResult result = userTokenService.createToken();
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());

        return new ModelAndView("redirect:/settings/invitation/");
    }

    @PostMapping("/redeem/{tokenId}")
    public ModelAndView redeemToken(@PathVariable("tokenId") UUID tokenId, RedirectAttributes redirectAttributes) {
        ServiceResult result = userTokenService.redeemToken(tokenId, System.currentTimeMillis());
        redirectAttributes.addFlashAttribute(VotingController.FLASH_MESSAGE, result.getMessageKey());

        return new ModelAndView("redirect:/settings/invitation/");
    }

}
