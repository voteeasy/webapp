package de.voteeasy.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantExportServiceTest {
    @Mock
    private TenantStatisticsService tenantStatisticsService;

    @InjectMocks
    private TenantExportService tenantExportService;

    @Test
    void injectionWorks() {
        assertThat(tenantExportService).isNotNull();
    }

    @Test
    void exportWithoutResponse() {
        assertDoesNotThrow(() -> tenantExportService.export(null, new HashSet<>()));
    }

    @Test
    void exportWithNullTenants() throws IOException {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        tenantExportService.export(response, null);
        verifyNoMoreInteractions(tenantStatisticsService);
    }

    @Test
    void exportWithNoTenantsYieldsNoException() {
        Tenant a = new Tenant("a");
        a.setId(UUID.randomUUID());
        final MockHttpServletResponse response = new MockHttpServletResponse();
        assertDoesNotThrow(() -> tenantExportService.export(response, Set.of(a)));
    }

    @Test
    void export() throws IOException {
        Tenant a = new Tenant("aa");
        a.setEnabled(false);
        a.setId(UUID.randomUUID());

        when(tenantStatisticsService.getActiveUserCount(eq(a))).thenReturn(1L);
        when(tenantStatisticsService.getUserTokenCount(eq(a))).thenReturn(2L);
        when(tenantStatisticsService.getAvailableUserTokenCount(eq(a))).thenReturn(3L);
        when(tenantStatisticsService.getDisabledUserTokenCount(eq(a))).thenReturn(4L);
        when(tenantStatisticsService.getAlreadyRedeemedUserTokenCount(eq(a))).thenReturn(5L);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        tenantExportService.export(response, Set.of(a));
        String resultingCsv = response.getContentAsString();
        assertThat(resultingCsv).isNotNull()
                .contains(",false,") // disabled tenant
                .contains(",1,2,3,4,5") // stats data
                .contains("aa") // tenant name
                .contains("Number,Name,CreatedAt,Enabled,Total Registered Users,Total UserToken,Available token including disabled tokens,Disabled token,Already redeemed token"); //headers
    }

}