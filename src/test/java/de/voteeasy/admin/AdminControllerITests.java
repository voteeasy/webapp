package de.voteeasy.admin;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerITests extends AbstractIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private VersionService versionService;

    @Test
    void privateApplicationPartGivesRedirectToLogin() throws Exception {
        mvc.perform(
                get("/admin/"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(
            username = "emil",
            roles = "USER"
    )
    void nonAdminAuthenticatedUserGives403AsWell() throws Exception {
        mvc.perform(
                get("/admin/cardinality"))
                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    @WithMockUser(
            username = "user",
            roles = {"USER", "ADMIN"}
    )
    void showCardinality() throws Exception {
        when(adminService.calculateCardinalities()).thenReturn(new CardinalityResult());
        mvc.perform(
                get("/admin/cardinality"))
                .andExpect(
                        status().isOk()
                ).andExpect(view().name("admin/cardinality")).andExpect(model().attribute("total", 0L)).andExpect(model().attributeExists("cardinalityMap")
        );

    }

    @Test
    @WithMockUser(
            username = "user",
            roles = {"USER", "ADMIN"}
    )
    void showVersionInformationDeliversTwoParts() throws Exception {
        when(versionService.getGitProperties()).thenReturn(Collections.emptyMap());
        when(versionService.getBuildProperties()).thenReturn(Collections.emptyMap());

        mvc.perform(
                get("/admin/version"))
                .andExpect(
                        status().isOk()
                ).andExpect(view().name("admin/version")).andExpect(model().attributeExists("versioninformation")).andExpect(model().attributeExists("versioninformation")
        );

    }
}
