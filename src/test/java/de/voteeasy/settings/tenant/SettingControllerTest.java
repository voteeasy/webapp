package de.voteeasy.settings.tenant;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
class SettingControllerTest extends AbstractIT {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SettingService settingService;
    @MockitoBean
    private ApplicationUserService applicationUserService;

    @Autowired
    private SettingController settingController;

    @Test
    void controllerIsWiredCorrectly() {
        assertThat(settingController).isNotNull();
    }

    @Test
    @WithMockUser(username = "user")
    void showMyPropertiesAsUser() throws Exception {
        Set<Setting> mySettings = Set.of(Setting.builder().build());
        when(settingService.getMySettings()).thenReturn(mySettings);

        mvc.perform(get("/tsettings/properties/").with(csrf())
        ).andExpect(status().isForbidden()
        );
    }

    @Test
    @WithMockUser(username = "tenantadmin", roles = {"USER", "ADMIN"})
    void showMyPropertiesAsAdmin() throws Exception {
        Set<Setting> mySettings = Set.of(Setting.builder().build());
        when(settingService.getMySettings()).thenReturn(mySettings);

        mvc.perform(get("/tsettings/properties/").with(csrf())
        ).andExpect(status().isOk()
        ).andExpect(view().name("tsettings/properties/myproperties.html")
        ).andExpect(model().attributeExists("properties")
        ).andExpect(model().attribute("properties", mySettings)
        );
    }

    @Test
    @WithMockUser(username = "globaladmin", roles = {"USER", "ADMIN", "TENANT_ADMIN"})
    void showMyPropertiesAsSuperAdmin() throws Exception {
        Set<Setting> mySettings = Set.of(Setting.builder().build());
        when(settingService.getMySettings()).thenReturn(mySettings);

        mvc.perform(get("/tsettings/properties/").with(csrf())
        ).andExpect(status().isOk()
        ).andExpect(view().name("tsettings/properties/myproperties.html")
        ).andExpect(model().attributeExists("properties")
        ).andExpect(model().attribute("properties", mySettings)
        );
    }

    @Test
    @WithMockUser(username = "emil")
    void userCannotShowMyProperties() throws Exception {
        mvc.perform(get("/tsettings/properties/" + UUID.randomUUID()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "globaladmin", roles = {"USER", "ADMIN", "TENANT_ADMIN"})
    void showPropertiesByTenantId() throws Exception {
        UUID tenantId = UUID.randomUUID();

        Set<Setting> properties = Set.of(Setting.builder().key("showPropertiesByTenantId").tenant(new Tenant()).build());
        when(settingService.getSettings(eq(tenantId))).thenReturn(properties);

        mvc.perform(get("/tsettings/properties/" + tenantId).with(csrf())
        ).andExpect(status().isOk()
        ).andExpect(view().name("tsettings/properties/properties.html")
        ).andExpect(model().attribute("properties", properties)
        );
    }

    @Test
    @WithMockUser(username = "user")
    void showPropertiesByTenantIdNotAllowedAsRegularUser() throws Exception {
        mvc.perform(get("/tsettings/properties/" + UUID.randomUUID()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "globaladmin", roles = {"USER", "ADMIN", "TENANT_ADMIN"})
    void showAvailableKeys() throws Exception {
        Set<SettingKeyUsage> keys = Set.of(SettingKeyUsage.builder().key("showAvailableKeys").count(123L).build());
        when(settingService.getAllKeys(eq(SettingScope.TENANT))).thenReturn(keys);

        mvc.perform(get("/tsettings/properties/keys").with(csrf())
        ).andExpect(status().isOk()
        ).andExpect(view().name("tsettings/properties/keys.html")
        ).andExpect(model().attribute("keys", keys)
        );
    }

    @Test
    @WithMockUser(username = "user")
    void showAvailableKeysIsUnavailableForRegularUsers() throws Exception {
        mvc.perform(get("/tsettings/properties/keys").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void showAddSettingFormIsUnavailableForRegularUsers() throws Exception {
        mvc.perform(get("/tsettings/properties/add/" + UUID.randomUUID()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "globaladmin", roles = {"USER", "ADMIN", "TENANT_ADMIN"})
    void showAddSettingForm() throws Exception {
        UUID tenantId = UUID.randomUUID();

        Tenant tenant = new Tenant("showAddSettingForm");
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(User.builder().organisation(tenant).build()));
        when(settingService.getTenantDetails(eq(tenantId))).thenReturn(Optional.of(tenant));

        mvc.perform(get("/tsettings/properties/add/" + tenantId).with(csrf())
        ).andExpect(status().isOk()
        ).andExpect(view().name("tsettings/properties/property-add.html")
        ).andExpect(model().attributeExists("setting")
        );
    }

    @Test
    @WithMockUser(username = "user")
    void submitAddSettingFormNotAllowedAsRegularUser() throws Exception {
        mvc.perform(post("/tsettings/properties/add/" + UUID.randomUUID()).with(csrf())
        ).andExpect(status().isForbidden()
        );
    }

    @Test
    @WithMockUser(username = "globaladmin", roles = {"USER", "ADMIN", "TENANT_ADMIN"})
    void submitAddSettingForm() throws Exception {
        mvc.perform(post("/tsettings/properties/add/" + UUID.randomUUID()).with(csrf())
        ).andExpect(status().isOk() // in this test we do not care if errors or success
        );
    }

    @Test
    @WithMockUser(username = "user")
    void deleteSettingNotAllowedAsRegularUser() throws Exception {
        mvc.perform(post("/tsettings/properties/delete/" + UUID.randomUUID() + "/" + UUID.randomUUID()).with(csrf())
        ).andExpect(status().isForbidden()
        );
    }

    @Test
    @WithMockUser(username = "globaladmin", roles = {"USER", "ADMIN", "TENANT_ADMIN"})
    void deleteSetting() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID settingId = UUID.randomUUID();

        ServiceResult result = ServiceResult.builder().status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).messageKey("deleteSetting").build();
        when(settingService.deleteProperty(eq(settingId))).thenReturn(result);

        mvc.perform(post("/tsettings/properties/delete/" + tenantId + "/" + settingId).with(csrf())
        ).andExpect(status().is3xxRedirection()
        ).andExpect(view().name("redirect:/tsettings/properties/" + tenantId)
        ).andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "deleteSetting")
        );
    }
}