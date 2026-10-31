package de.voteeasy.invitation;

import de.voteeasy.AbstractIT;
import de.voteeasy.export.UserTokenCSVService;
import de.voteeasy.export.UserTokenExcelService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NON_AUTHORITATIVE_INFORMATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserTokenControllerTest extends AbstractIT {

    @MockitoBean
    private UserTokenService userTokenService;

    @MockitoBean
    private ApplicationUserService applicationUserService;

    @MockitoBean
    private UserTokenCSVService userTokenCSVService;

    @MockitoBean
    private UserTokenExcelService userTokenExcelService;

    @MockitoBean
    private TenantService tenantService;

    @Autowired
    private MockMvc mvc;

    @InjectMocks
    private UserTokenController userTokenController;

    @Test
    void controllerIsCorrectlyWired() {
        assertThat(userTokenController).isNotNull();
        assertThat(mvc).isNotNull();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void exportResultsAsCSV() throws Exception {
        when(userTokenService.getUsableToken()).thenReturn(Sets.newSet(UserToken.builder().build()));
        mvc.perform(get("/settings/invitation/export"))
                .andExpect(status().isOk());

        verify(userTokenService).getUsableToken();
        verify(userTokenCSVService).export(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void exportResultsAsExcel() throws Exception {
        when(userTokenService.getUsableToken()).thenReturn(Sets.newSet(UserToken.builder().build()));
        when(userTokenExcelService.downloadAsExcel(any())).thenReturn(new ByteArrayInputStream("Test".getBytes(StandardCharsets.UTF_8)));

        mvc.perform(get("/settings/invitation/exportXLS"))
                .andExpect(status().isOk());

        verify(userTokenService).getUsableToken();
        verify(userTokenExcelService).export(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listWithoutLoggedInUserRedirectsToWelcomePage() throws Exception {
        mvc.perform(get("/settings/invitation/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listWithExistingTokens() throws Exception {
        when(userTokenService.getToken()).thenReturn(Sets.newSet(UserToken.builder().build()));
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(new User()));

        mvc.perform(get("/settings/invitation/"))
                .andExpect(status().isOk())
                .andExpect(view().name("/settings/invitation/index.html"))
                .andExpect(model().attributeExists("tokens", "redeemedCount", "openCount", "disabledCount"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listWithProtectedTenant() throws Exception {
        when(userTokenService.getToken()).thenReturn(Sets.newSet(UserToken.builder().build()));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(true);
        User u = new User();
        u.setOrganisation(new Tenant("listWithProtectedTenant"));
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(u));

        mvc.perform(get("/settings/invitation/"))
                .andExpect(status().isOk())
                .andExpect(view().name("/settings/invitation/index.html"))
                .andExpect(model().attributeExists("tokens", "redeemedCount", "openCount", "disabledCount"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listTriggersInitialTokenGeneration() throws Exception {
        when(userTokenService.getToken()).thenReturn(Collections.emptySet());
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(new User()));
        when(userTokenService.createToken()).thenReturn(ServiceResult.builder().messageKey("initialized").status(NON_AUTHORITATIVE_INFORMATION).build());

        mvc.perform(get("/settings/invitation/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "initialized"))
                .andExpect(redirectedUrl("/settings/invitation/"))
        ;

        verify(userTokenService).createToken();
        verify(userTokenService).getToken();
        // once called from advice in controller and to get current participation
        verify(applicationUserService, atLeast(2)).currentlyLoggedInUser();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggle() throws Exception {
        UUID tokenId = UUID.randomUUID();
        when(userTokenService.toggleToken(eq(tokenId))).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("success").build());

        mvc.perform(post("/settings/invitation/toggle/" + tokenId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success"))
                .andExpect(redirectedUrl("/settings/invitation/"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteToken() throws Exception {
        UUID tokenId = UUID.randomUUID();
        when(userTokenService.deleteToken(eq(tokenId))).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("success").build());

        mvc.perform(post("/settings/invitation/delete/" + tokenId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success"))
                .andExpect(redirectedUrl("/settings/invitation/"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createTokens() throws Exception {
        when(userTokenService.createToken()).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("success").build());

        mvc.perform(get("/settings/invitation/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success"))
                .andExpect(redirectedUrl("/settings/invitation/"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void redeemToken() throws Exception {
        UUID tokenId = UUID.randomUUID();
        when(userTokenService.redeemToken(eq(tokenId), any(Long.class))).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("success").build());

        mvc.perform(post("/settings/invitation/redeem/" + tokenId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success"))
                .andExpect(redirectedUrl("/settings/invitation/"));
    }
}