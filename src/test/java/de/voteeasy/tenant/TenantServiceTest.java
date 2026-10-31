package de.voteeasy.tenant;

import de.voteeasy.serviceresult.ServiceResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    static final UUID TENANT_ID = UUID.randomUUID();
    static final String TENANT_NAME = "TenantServiceTest";

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    static Tenant getTenant(boolean isProtected) {
        Tenant t = new Tenant(isProtected ? "VoteEasy" : TENANT_NAME);
        t.setId(TENANT_ID);
        t.setCreatedAt(1L);
        t.setEnabled(false);
        return t;
    }

    @Test
    void ensureOverduePeriodIsNotChangedAccidentally() {
        assertThat(TenantService.OVERDUE_PERIOD).isEqualTo(60 * 86400000L);
    }

    @Test
    void injectionWorkedProperly() {
        assertThat(tenantService).isNotNull();
    }

    @Test
    void list() {
        when(tenantRepository.findAll(eq(Sort.by("name")))).thenReturn(List.of(getTenant(false)));

        final Set<Tenant> tenants = tenantService.list();
        Assertions.assertThat(tenants).contains(getTenant(false));
    }

    @Test
    void deleteTenantWithUnknownTenantId() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        final ServiceResult serviceResult = tenantService.deleteTenant(TENANT_ID);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void deleteTenantThatIsProtectedIsForbidden() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(getTenant(true)));

        final ServiceResult serviceResult = tenantService.deleteTenant(TENANT_ID);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.tenant.protected");
    }

    @Test
    void toggleTenantWithUnknownTenantId() {
        final ServiceResult serviceResult = tenantService.toggleTenant(UUID.randomUUID());
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(NOT_FOUND);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void toggleTenantThatIsProtectedIsForbidden() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(getTenant(true)));
        final ServiceResult serviceResult = tenantService.toggleTenant(TENANT_ID);

        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(FORBIDDEN);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.tenant.protected");
    }

    @Test
    void toggleTenant() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(getTenant(false)));
        final ServiceResult serviceResult = tenantService.toggleTenant(TENANT_ID);

        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(OK);
        // return code encodes state of tenant after toggling
        assertThat(serviceResult.getMessageKey()).isEqualTo("success.tenant.on");
        verify(tenantRepository).save(any());
    }

    @Test
    void isProtectedIsNullSafe() {
        assertThat(tenantService.isProtected(null)).isFalse();
    }

    @Test
    void isProtectedReturnsTrue() {
        assertThat(tenantService.isProtected(new Tenant("VoteEasy"))).isTrue();
    }

    @Test
    void isProtectedReturnsFalse() {
        assertThat(tenantService.isProtected(new Tenant("UnknownTenant"))).isFalse();
    }
}