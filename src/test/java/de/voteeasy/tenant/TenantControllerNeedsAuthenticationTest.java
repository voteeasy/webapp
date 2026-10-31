package de.voteeasy.tenant;

import de.voteeasy.AbstractIT;
import de.voteeasy.user.ApplicationUserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TenantControllerNeedsAuthenticationTest extends AbstractIT {
    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private ApplicationUserService applicationUserService;

    @Autowired
    private MockMvc mvc;

    @InjectMocks
    private TenantController tenantController;

    @Test
    public void controllerIsCorrectlyWired() {
        assertThat(tenantController).isNotNull();
        assertThat(mvc).isNotNull();
    }

    @Test
    void listNeedsAuth() throws Exception {
        mvc.perform(get("/settings/tenant/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void createTenantNeedsAuth() throws Exception {
        mvc.perform(post("/settings/tenant/add")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void showCreateTenantFormNeedsAuth() throws Exception {
        mvc.perform(get("/settings/tenant/add")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void toggleTenantNeedsAuth() throws Exception {
        mvc.perform(post("/settings/tenant/toggle/" + UUID.randomUUID())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void deleteTenantNeedsAuth() throws Exception {
        mvc.perform(post("/settings/tenant/delete/" + UUID.randomUUID())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void exportNeedsAuth() throws Exception {
        mvc.perform(get("/settings/tenant/export")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }
}