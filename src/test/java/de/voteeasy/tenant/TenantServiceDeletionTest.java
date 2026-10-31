package de.voteeasy.tenant;

import de.voteeasy.gathering.GatheringService;
import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.settings.tenant.SettingRepository;
import de.voteeasy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.UUID;

import static de.voteeasy.tenant.TenantServiceTest.TENANT_ID;
import static de.voteeasy.tenant.TenantServiceTest.getTenant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceDeletionTest {
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private GatheringService gatheringService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private SettingRepository settingRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void injectionWorkedProperly() {
        assertThat(tenantService).isNotNull();
    }

    @Test
    void deleteTenant() {
        Tenant tenant = getTenant(false);
        when(tenantRepository.findById(eq(TENANT_ID))).thenReturn(Optional.of(tenant));
        final ServiceResult serviceResult = tenantService.deleteTenant(TENANT_ID);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getMessageKey()).isEqualTo("success.tenant.remove");
        assertThat(serviceResult.getStatus()).isEqualTo(OK);

        // verify that all repositories had some sort of delete operations ;)
        verify(tenantRepository).findById(eq(TENANT_ID));
        verify(userTokenRepository).findAllByOrganisation(eq(tenant), any(Sort.class));
        verify(userTokenRepository).deleteAll(any());
        verify(settingRepository).deleteAllByTenant(eq(tenant));
        // referenced repositories are handled in tests of that method
        verify(gatheringService).deleteCascadinglyWithoutTenantCheck(eq(tenant));
        verify(userRepository).deleteAllByOrganisation(eq(tenant));
        verify(tenantRepository).delete(eq(tenant));
    }

    @Test
    void deleteProtectedTenantIsNotWorking() {
        when(tenantRepository.findById(any())).thenReturn(Optional.of(getTenant(true)));
        final ServiceResult serviceResult = tenantService.deleteTenant(UUID.randomUUID());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.tenant.protected");
        assertThat(serviceResult.getStatus()).isEqualTo(FORBIDDEN);
    }

    @Test
    void deleteTenantWithUnknownId() {
        when(tenantRepository.findById(any())).thenReturn(Optional.empty());
        final ServiceResult serviceResult = tenantService.deleteTenant(UUID.randomUUID());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.unknownId");
        assertThat(serviceResult.getStatus()).isEqualTo(NOT_FOUND);
    }
}