package de.voteeasy.settings.tenant;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.tenant.TenantService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {
    @Mock
    private SettingRepository settingRepository;
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private TenantService tenantService;

    @InjectMocks
    private SettingService settingService;

    @Test
    void wiringWorked() {
        assertThat(settingService).isNotNull();
    }

    @Test
    void getSettingsEmptyWithoutUser() {
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(any())).thenReturn(Optional.empty());
        assertThat(settingService.getMySettings()).isEmpty();
    }

    @Test
    void getSettingsForCurrentlyLoggedInTenant() {
        Tenant tenant = new Tenant("getSettingsForTenant");
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("ADMIN"))).thenReturn(Optional.of(User.builder().organisation(tenant).build()));
        Setting s = Setting.builder().key("key").type(SettingType.UUID).scope(SettingScope.TENANT).value(UUID.randomUUID().toString()).build();
        when(settingRepository.getAllByTenant(eq(tenant))).thenReturn(Sets.newTreeSet(s));

        assertThat(settingService.getMySettings()).containsExactly(s);
    }

    @Test
    void getSettingsSpecifiedByTenant() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant("getSettingsSpecifiedByTenant");
        tenant.setId(tenantId);

        when(tenantService.getTenant(eq(tenantId))).thenReturn(Optional.of(tenant));

        Setting s = Setting.builder().key("key").type(SettingType.UUID).scope(SettingScope.TENANT).value(UUID.randomUUID().toString()).build();
        when(settingRepository.getAllByTenant(eq(tenant))).thenReturn(Sets.newTreeSet(s));

        assertThat(settingService.getSettings(tenantId)).containsExactly(s);
    }

    @Test
    void getSettingsByUnknownTenantId() {
        assertThat(settingService.getSettings(UUID.randomUUID())).isEmpty();
    }

    @Test
    void getSettingsByUnknownTenant() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant("getSettingsSpecifiedByTenant");
        tenant.setId(tenantId);

        when(tenantService.getTenant(eq(tenantId))).thenReturn(Optional.of(tenant));
        assertThat(settingService.getSettings(tenantId)).isEmpty();
    }

    @Test
    void getAllKeysIsNullsafe() {
        assertThat(settingService.getAllKeys(null)).isEmpty();
        verify(settingRepository).getAllByScope(eq(null));
    }

    @Test
    void getAllKeysWorksForAllScopes() {
        for (SettingScope s : SettingScope.values()) {
            assertThat(settingService.getAllKeys(s)).isEmpty();
            verify(settingRepository).getAllByScope(eq(s));
        }
    }

    @Test
    void deleteBySettingsId() {
        UUID settingId = UUID.randomUUID();
        final ServiceResult serviceResult = settingService.deleteProperty(settingId);
        assertThat(serviceResult).isNotNull();
        verify(settingRepository).deleteById(eq(settingId));
    }

    @Test
    void getTenantDetailsForUnknownId() {
        assertThat(settingService.getTenantDetails(UUID.randomUUID())).isEmpty();
    }

    @Test
    void getTenantDetails() {
        UUID tenantId = UUID.randomUUID();
        Tenant t = new Tenant("getTenantDetailsTest");
        t.setId(tenantId);
        when(tenantService.getTenant(eq(tenantId))).thenReturn(Optional.of(t));

        assertThat(settingService.getTenantDetails(tenantId)).contains(t);
    }

    @Test
    void getSettingWithKeyNull() {
        assertThat(settingService.getSetting(null, SettingScope.TENANT, new Tenant("getSettingWithKeyNull"))).isEmpty();
    }

    @Test
    void getSettingWithKeyAndNoScope() {
        String key = "asdljaldjkads,ymx";
        Tenant t = new Tenant("getSettingWithKeyAndNoScope");
        assertThat(settingService.getSetting(key, null, t)).isEmpty();
        verify(settingRepository).findAllByKeyIgnoreCaseAndScopeAndTenant(eq(key), eq(SettingScope.TENANT), eq(t));
    }

    @Test
    void getSettingWithKeyAndScope() {
        String key = "8765rtzghfx";
        Tenant t = new Tenant("getSettingWithKeyAndScope");
        assertThat(settingService.getSetting(key, SettingScope.USER, t)).isEmpty();
        verify(settingRepository).findAllByKeyIgnoreCaseAndScopeAndTenant(eq(key), eq(SettingScope.USER), eq(t));
    }

    @Test
    void getSettingWithKeyAndScopeAndTooManyResults() {
        String key = "564732ztqwghsbjj";
        Tenant t = new Tenant("getSettingWithKeyAndScopeAndTooManyResults");
        // we need to manipulate the key to have more than one result, which indicates an internal database error
        Setting s1 = Setting.builder().key(key + "0").value("v3").id(UUID.randomUUID()).scope(SettingScope.USER).type(SettingType.ALPHANUMERIC).build();
        Setting s2 = Setting.builder().key(key + "1").value("v2").id(UUID.randomUUID()).scope(SettingScope.USER).type(SettingType.ALPHANUMERIC).build();
        Setting s3 = Setting.builder().key(key + "2").value("v1").id(UUID.randomUUID()).scope(SettingScope.USER).type(SettingType.ALPHANUMERIC).build();
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(eq(key), eq(SettingScope.USER), eq(t))).thenReturn(Sets.newTreeSet(s3, s1, s2));

        assertThat(settingService.getSetting(key, SettingScope.USER, t)).contains(s1);
        verify(settingRepository).findAllByKeyIgnoreCaseAndScopeAndTenant(eq(key), eq(SettingScope.USER), eq(t));
    }

    @Test
    void getSettingWithKeyAndScopeReturnsExactResult() {
        String key = "7654hggfes";
        Tenant t = new Tenant("getSettingWithKeyAndScopeReturnsExactResult");
        Setting s1 = Setting.builder().key(key).value("v1").id(UUID.randomUUID()).type(SettingType.ALPHANUMERIC).build();
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(eq(key), eq(SettingScope.USER), eq(t))).thenReturn(Sets.newTreeSet(s1));

        assertThat(settingService.getSetting(key, SettingScope.USER, t)).contains(s1);
        verify(settingRepository).findAllByKeyIgnoreCaseAndScopeAndTenant(eq(key), eq(SettingScope.USER), eq(t));
    }

    @Test
    void addPropertyCallerIsNotAdmin() {
        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().userId(UUID.randomUUID()).build(), new Tenant());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(METHOD_NOT_ALLOWED);
    }

    @Test
    void addPropertyFormContainsNoUserId() {
        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().build(), new Tenant());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(METHOD_NOT_ALLOWED);
    }

    @Test
    void addPropertyFormUserIdDoesNotMatchCurrentAdminUserId() {
        UUID uId = UUID.randomUUID();
        User u = User.builder().id(uId).build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(u));

        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().build(), new Tenant());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(METHOD_NOT_ALLOWED);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void addPropertyKeyAlreadySet() {
        UUID uId = UUID.randomUUID();
        User u = User.builder().id(uId).build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(u));

        Tenant t = new Tenant("addPropertyKeyAlreadySet");
        Setting s1 = Setting.builder().key("key").value("v1").id(UUID.randomUUID()).type(SettingType.ALPHANUMERIC).build();
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(anyString(), eq(SettingScope.TENANT), eq(t))).thenReturn(Sets.newTreeSet(s1));

        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().key("key").scope("TENANT").userId(uId).build(), t);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(CONFLICT);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.propertyexists");
    }

    @Test
    void addPropertyNoTenantGiven() {
        UUID uId = UUID.randomUUID();
        User u = User.builder().id(uId).build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(u));

        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().key("key").scope("TENANT").userId(uId).build(), null);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(METHOD_NOT_ALLOWED);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.unknownId");
        verifyNoMoreInteractions(settingRepository);
    }

    @Test
    void addPropertyTenantIsProtectedNoSettingsAllowed() {
        UUID uId = UUID.randomUUID();
        User u = User.builder().id(uId).build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(u));

        final Tenant t = new Tenant();
        when(tenantService.isProtected(eq(t))).thenReturn(true);
        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().key("key").scope("TENANT").userId(uId).build(), t);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.tenant.protected");
        verifyNoMoreInteractions(settingRepository);
    }

    @Test
    void addPropertyWorks() {
        UUID uId = UUID.randomUUID();
        User u = User.builder().id(uId).build();
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(anyString())).thenReturn(Optional.of(u));

        final Tenant t = new Tenant();
        final ServiceResult serviceResult = settingService.addProperty(SettingPropertyForm.builder().key("key").scope("TENANT").userId(uId).build(), t);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(OK);
        assertThat(serviceResult.getMessageKey()).isEqualTo("property.create.success");
        verify(settingRepository).save(any(Setting.class));
    }
}