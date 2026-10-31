package de.voteeasy.invitation;

import de.voteeasy.AbstractIT;
import de.voteeasy.user.ApplicationUserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserTokenControllerNeedsAuthenticationTest extends AbstractIT {
    @MockitoBean
    private UserTokenService userTokenService;

    @MockitoBean
    private ApplicationUserService applicationUserService;

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
    void exportNeedsAuth() throws Exception {
        mvc.perform(get("/settings/invitation/export"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void exportAsExcelNeedsAuth() throws Exception {
        mvc.perform(get("/settings/invitation/exportXLS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void listNeedsAuth() throws Exception {
        mvc.perform(get("/settings/invitation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser("regularuser")
    void listNeedsAuthWithCorrectPermissions() throws Exception {
        mvc.perform(get("/settings/invitation/"))
                .andExpect(status().isForbidden());
    }

    @Test
    void toggleNeedsAuth() throws Exception {
        mvc.perform(post("/settings/invitation/toggle/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser("regularuser")
    void toggleNeedsAuthWithCorrectPermissions() throws Exception {
        mvc.perform(post("/settings/invitation/toggle/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTokenAuth() throws Exception {
        mvc.perform(post("/settings/invitation/delete/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser("regularuser")
    void deleteTokenAuthWithCorrectPermissions() throws Exception {
        mvc.perform(post("/settings/invitation/delete/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTokensAuth() throws Exception {
        mvc.perform(get("/settings/invitation/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser("regularuser")
    void createTokensAuthWithCorrectPermissions() throws Exception {
        mvc.perform(get("/settings/invitation/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    void redeemTokenAuth() throws Exception {
        mvc.perform(post("/settings/invitation/redeem/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser("regularuser")
    void redeemTokenAuthWithCorrectPermissions() throws Exception {
        mvc.perform(post("/settings/invitation/redeem/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}