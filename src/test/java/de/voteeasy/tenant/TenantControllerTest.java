package de.voteeasy.tenant;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.votes.VotingController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TenantControllerTest extends AbstractIT {

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private ApplicationUserService applicationUserService;

    @MockitoBean
    private TenantExportService tenantExportService;

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
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void listWithoutLoggedInUserRedirectsToWelcomePage() throws Exception {
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.empty());

        mvc.perform(get("/settings/tenant/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verifyNoInteractions(tenantService);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void list() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));

        Set<Tenant> tenants = Sets.newSet(new Tenant("Testing"));
        when(tenantService.list()).thenReturn(tenants);

        mvc.perform(get("/settings/tenant/"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tenant/index.html"))
                .andExpect(model().attribute("tenants", tenants))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void showCreateTenantForm() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));

        mvc.perform(get("/settings/tenant/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tenant/add.html"))
                .andExpect(model().attribute("tenant", CreateTenantForm.builder().build()))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void showCreateTenantFormWithoutBeingLoggedInProperly() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.empty());

        mvc.perform(get("/settings/tenant/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(model().attributeDoesNotExist("tenant"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void createTenantWithFormErrors() throws Exception {
        CreateTenantForm emptyForm = CreateTenantForm.builder().build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));

        mvc.perform(post("/settings/tenant/add")
                        .with(csrf()).content(emptyForm.toString())
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("tenant", "adminPassword", "NotEmpty"))
                .andExpect(model().attributeHasFieldErrorCode("tenant", "name", "NotEmpty"))
                .andExpect(view().name("settings/tenant/add"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void createTenantWithPasswordFieldTooShortError() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));
        when(tenantService.createTenant(any(CreateTenantForm.class))).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("tenantCreated").build());

        mvc.perform(post("/settings/tenant/add")
                        .with(csrf())
                        .param("name", "submittedName")
                        .param("adminPassword", "123")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("tenant", "adminPassword", "Size"))
                .andExpect(view().name("settings/tenant/add"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void createTenantWithTenantThatAlreadyExists() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));
        when(tenantService.createTenant(any(CreateTenantForm.class))).thenReturn(ServiceResult.builder().status(HttpStatus.FORBIDDEN).messageKey("tenantAlreadyExists").build());

        mvc.perform(post("/settings/tenant/add")
                        .with(csrf())
                        .param("name", "submittedName")
                        .param("adminPassword", "pwpwpw")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("tenant", "name"))
                .andExpect(view().name("settings/tenant/add"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void createTenant() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));
        when(tenantService.createTenant(any(CreateTenantForm.class))).thenReturn(ServiceResult.builder().status(HttpStatus.OK).messageKey("created").build());

        mvc.perform(post("/settings/tenant/add")
                        .with(csrf())
                        .param("name", "submittedName")
                        .param("adminPassword", "longlonglong")
                ).andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "created"))
                .andExpect(redirectedUrl("/settings/tenant/"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void toggleTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));
        when(tenantService.toggleTenant(eq(tenantId))).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("success").build());

        mvc.perform(post("/settings/tenant/toggle/" + tenantId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success"))
                .andExpect(redirectedUrl("/settings/tenant/"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void deleteTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));
        when(tenantService.deleteTenant(eq(tenantId))).thenReturn(ServiceResult.builder().status(HttpStatus.NOT_ACCEPTABLE).messageKey("success").build());

        mvc.perform(post("/settings/tenant/delete/" + tenantId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success"))
                .andExpect(redirectedUrl("/settings/tenant/"))
        ;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
    void export() throws Exception {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("TENANT_ADMIN"))).thenReturn(Optional.of(new User()));

        mvc.perform(get("/settings/tenant/export"))
                .andExpect(status().isOk())
        ;
        verify(tenantService).list();
        verify(tenantExportService).export(any(), any());
    }
}