package de.voteeasy.tenant;

import de.voteeasy.invitation.UserTokenRepository;
import de.voteeasy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantStatisticsServiceTest {
    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TenantStatisticsService tenantStatisticsService;

    @Test
    void injectionWorks() {
        assertThat(tenantStatisticsService).isNotNull();
    }

    @Test
    void getUserTokenCount() {
        Tenant t = new Tenant("getUserTokenCount");
        assertThat(tenantStatisticsService.getUserTokenCount(t)).isEqualTo(0);

        when(userTokenRepository.countAllByOrganisation(eq(t))).thenReturn(4711L);
        assertThat(tenantStatisticsService.getUserTokenCount(t)).isEqualTo(4711L);
    }

    @Test
    void getActiveUserCount() {
        Tenant t = new Tenant("getActiveUserCount");
        assertThat(tenantStatisticsService.getActiveUserCount(t)).isEqualTo(0);

        when(userRepository.countAllByOrganisation(eq(t))).thenReturn(4711L);
        assertThat(tenantStatisticsService.getActiveUserCount(t)).isEqualTo(4711L);
    }

    @Test
    void getAvailableUserTokenCount() {
        Tenant t = new Tenant("getAvailableUserTokenCount");
        assertThat(tenantStatisticsService.getAvailableUserTokenCount(t)).isEqualTo(0);

        when(userTokenRepository.countByRedeemedAtIsNullAndOrganisation(eq(t))).thenReturn(4711L);
        assertThat(tenantStatisticsService.getAvailableUserTokenCount(t)).isEqualTo(4711L);
    }

    @Test
    void getDisabledUserTokenCount() {
        Tenant t = new Tenant("getDisabledUserTokenCount");
        assertThat(tenantStatisticsService.getDisabledUserTokenCount(t)).isEqualTo(0);

        when(userTokenRepository.countByEnabledIsFalseAndOrganisation(eq(t))).thenReturn(4711L);
        assertThat(tenantStatisticsService.getDisabledUserTokenCount(t)).isEqualTo(4711L);
    }

    @Test
    void getAlreadyRedeemedUserTokenCount() {
        Tenant t = new Tenant("getAlreadyRedeemedUserTokenCount");
        assertThat(tenantStatisticsService.getAlreadyRedeemedUserTokenCount(t)).isEqualTo(0);

        when(userTokenRepository.countByRedeemedAtIsNotNullAndOrganisation(eq(t))).thenReturn(4711L);
        assertThat(tenantStatisticsService.getAlreadyRedeemedUserTokenCount(t)).isEqualTo(4711L);
    }
}