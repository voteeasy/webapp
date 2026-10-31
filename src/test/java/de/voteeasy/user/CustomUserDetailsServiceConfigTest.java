package de.voteeasy.user;

import de.voteeasy.tenant.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static de.voteeasy.login.LoginTenantAuthenticationFilter.toUserNameWithTenant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceConfigTest {
    private static final String PASSWORD = "pw";
    private static final String USERNAME = "u";
    private static final String TENANTNAME = "t";
    private static final String ROLENAME = "r";
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsServiceConfig userDetailsServiceConfig;

    private UserDetailsService userDetailsService;

    @BeforeEach
    public void getService() {
        this.userDetailsService = userDetailsServiceConfig.voteEasyUserDetailsService(userRepository);
    }

    @Test
    public void wiringWorks() {
        assertThat(userDetailsServiceConfig).isNotNull();
        assertThat(userDetailsService).isNotNull();
    }

    @Test
    public void notFoundIsHandled() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("notFound"));
    }

    @Test
    public void foundByUsernameButCorruptTenant() {
        assertThrows(UsernameNotFoundException.class, () -> {
            User user = User.builder().userName(USERNAME).organisation(new Tenant()).build();
            when(userRepository.getForLogin(eq(USERNAME), any())).thenReturn(Optional.of(user));

            userDetailsService.loadUserByUsername(toUserNameWithTenant(USERNAME, TENANTNAME));
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void foundByUserNameWithTenantButNoGrantsDefined(Collection<Role> roles) {
        assertThrows(UsernameNotFoundException.class, () -> {
            Tenant tenant = new Tenant(TENANTNAME);
            UUID tenantId = UUID.randomUUID();
            tenant.setId(tenantId);
            User user = User.builder().userName(USERNAME).organisation(tenant).roles(roles).passwordHash(PASSWORD).build();
            when(userRepository.getForLogin(eq(USERNAME), eq(TENANTNAME))).thenReturn(Optional.of(user));

            userDetailsService.loadUserByUsername(toUserNameWithTenant(USERNAME, TENANTNAME));
        });
    }

    @Test
    public void foundByUserNameWithTenantAndGrantsIsMapped() {
        Tenant tenant = new Tenant(TENANTNAME);
        UUID tenantId = UUID.randomUUID();
        tenant.setId(tenantId);
        Role role = new Role(ROLENAME);
        User user = User.builder().userName(USERNAME).organisation(tenant).roles(Sets.newSet(role)).passwordHash(PASSWORD).build();
        when(userRepository.getForLogin(eq(USERNAME), eq(TENANTNAME))).thenReturn(Optional.of(user));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(toUserNameWithTenant(USERNAME, TENANTNAME));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
        assertThat(userDetails).isInstanceOf(ApplicationUser.class);
        ApplicationUser applicationUser = (ApplicationUser) userDetails;

        assertThat(applicationUser.getUsername()).isEqualTo(USERNAME);
        assertThat(applicationUser.getTenant()).isEqualTo(UserTenant.builder().tenantId(tenantId).tenantName(TENANTNAME).build());
        assertThat(applicationUser.getPassword()).isEqualTo(PASSWORD);
        assertThat(applicationUser.getAuthorities()).hasSize(1);
    }

    @Test
    public void loginNotPossibleIfTenantIsDisabled() {
        Tenant tenant = new Tenant(TENANTNAME);
        UUID tenantId = UUID.randomUUID();
        tenant.setId(tenantId);
        tenant.setEnabled(false);

        Role role = new Role(ROLENAME);
        User user = User.builder().userName(USERNAME).organisation(tenant).roles(Sets.newSet(role)).passwordHash(PASSWORD).build();
        when(userRepository.getForLogin(eq(USERNAME), eq(TENANTNAME))).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(toUserNameWithTenant(USERNAME, TENANTNAME)));
    }
}