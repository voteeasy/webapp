package de.voteeasy.settings.tenant;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettingServiceITest extends AbstractIT {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ApplicationUserService applicationUserService;
    @MockitoBean
    private SettingRepository settingRepository;
    @MockitoBean
    private TenantService tenantService;
    
    @Autowired
    private SettingService settingService;

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void getTenantKey() {
        assertThat(settingService.getTenantSetting("foo", null)).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void getKey() {
        assertThat(settingService.getSetting("foo", SettingScope.USER, null)).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    void getSettings() {
        assertThat(settingService.getMySettings()).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void deleteBySettingsIdIsNullSafe() {
        final ServiceResult serviceResult = settingService.deleteProperty(null);
        assertThat(serviceResult).isNotNull();
    }

    @Test
    public void privateApplicationPartGivesRedirectToLogin() throws Exception {
        mvc.perform(
                get("http://localhost/tsettings/properties/"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("http://localhost/login"));
    }
}