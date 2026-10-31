package de.voteeasy.settings.tenant;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadSettingServiceTest {
    @Mock
    private SettingRepository settingRepository;
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private TenantService tenantService;

    @InjectMocks
    private ReadSettingService readSettingService;

    @Test
    void mocksAreInjected() {
        assertThat(readSettingService).isNotNull();
    }

    @Test
    void getIntegerOrDefault() {
        User u = User.builder().organisation(new Tenant("getIntegerOrDefault")).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(false);
        Setting setting = Setting.builder().type(SettingType.NUMERIC).key("getIntegerOrDefault").value("4711").scope(SettingScope.TENANT).build();
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(anyString(), any(SettingScope.class), any(Tenant.class))).thenReturn(Sets.newTreeSet(setting));

        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefault", -7, null)).isEqualTo(Integer.parseInt(setting.getValue()));
    }

    @Test
    void getIntegerOrDefaultReturnsDefaultIfUserIsNotLoggedIn() {
        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefaultReturnsDefaultIfUserIsNotLoggedIn", -7, null)).isEqualTo(-7);
    }

    @Test
    void getIntegerOrDefaultReturnsDefaultIfTenantIsProtected() {
        User u = User.builder().organisation(new Tenant("getIntegerOrDefaultReturnsDefaultIfTenantIsProtected")).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(true);
        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefaultReturnsDefaultIfTenantIsProtected", -7, null)).isEqualTo(-7);
    }

    @Test
    void getIntegerOrDefaultReturnsUnparseableValueFromDatabaseThusDefault() {
        User u = User.builder().organisation(new Tenant("getIntegerOrDefaultReturnsUnparseableValueFromDatabaseThusDefault")).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(false);
        Setting invalidSetting = Setting.builder().type(SettingType.NUMERIC).key("getIntegerOrDefaultReturnsUnparseableValueFromDatabaseThusDefault").value("NaN").scope(SettingScope.TENANT).build();
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(anyString(), any(SettingScope.class), any(Tenant.class))).thenReturn(Sets.newTreeSet(invalidSetting));

        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefaultReturnsUnparseableValueFromDatabaseThusDefault", -7, null)).isEqualTo(-7);
    }

    @Test
    void getIntegerOrDefaultReturnsNoValueFromDatabaseThusDefault() {
        User u = User.builder().organisation(new Tenant("getIntegerOrDefaultReturnsNoValueFromDatabaseThusDefault")).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(false);
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(anyString(), any(SettingScope.class), any(Tenant.class))).thenReturn(Collections.emptySet());

        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefaultReturnsNoValueFromDatabaseThusDefault", -7, null)).isEqualTo(-7);
    }

    @Test
    void getIntegerOrDefaultReturnsDefaultValueIfDatabaseEntryIsNotNumeric() {
        User u = User.builder().organisation(new Tenant("getIntegerOrDefaultReturnsDefaultValueIfDatabaseEntryIsNotNumeric")).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(false);
        Set<Setting> databaseEntry = new HashSet<>();
        databaseEntry.add(Setting.builder().type(SettingType.UUID).build());
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(anyString(), any(SettingScope.class), any(Tenant.class))).thenReturn(databaseEntry);

        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefaultReturnsDefaultValueIfDatabaseEntryIsNotNumeric", -876, null)).isEqualTo(-876);
    }

    @Test
    void getIntegerOrDefaultReturnsValueFromDatabaseWithScope() {
        User u = User.builder().organisation(new Tenant("getIntegerOrDefaultReturnsValueFromDatabaseWithScope")).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        when(tenantService.isProtected(any(Tenant.class))).thenReturn(false);
        Set<Setting> databaseEntry = new HashSet<>();
        databaseEntry.add(Setting.builder().type(SettingType.NUMERIC).value("12345").build());
        when(settingRepository.findAllByKeyIgnoreCaseAndScopeAndTenant(anyString(), any(SettingScope.class), any(Tenant.class))).thenReturn(databaseEntry);

        assertThat(readSettingService.getIntegerOrDefault("getIntegerOrDefaultReturnsValueFromDatabaseWithScope", -876, SettingScope.USER)).isEqualTo(12345);
    }

}