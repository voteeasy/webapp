package de.voteeasy.login;

import de.voteeasy.participation.ParticipationService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUser;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import de.voteeasy.user.UserTenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ALREADY_REPORTED;

@ExtendWith(MockitoExtension.class)
class LeaveEventsUponLogoutSuccessHandlerTest {
    @Mock
    private ParticipationService participationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LeaveEventsUponLogoutSuccessHandler leaveEventsUponLogoutSuccessHandler;

    static Collection<GrantedAuthority> getAdminAndUserAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return grantedAuthorities;
    }

    @Test
    void wiringWorksOkay() {
        assertThat(leaveEventsUponLogoutSuccessHandler).isNotNull();
    }

    @Test
    void onLogoutSuccessPerformsLogoutAndEndsAllParticipations() throws IOException, ServletException {
        when(request.getContextPath()).thenReturn("UNDERTEST");
        ApplicationUser principal = new ApplicationUser(UserTenant.builder().tenantId(UUID.randomUUID()).tenantName("test").build(), "username", "password", getAdminAndUserAuthorities());
        when(authentication.getPrincipal()).thenReturn(principal);

        User u = new User("username", "fullname", "pwHash", new Tenant("test"));
        when(userRepository.getForLogin(eq("username"), eq("test"))).thenReturn(Optional.of(u));
        when(participationService.leave(eq(u))).thenReturn(ServiceResult.builder().messageKey("logoutSuccess").status(ALREADY_REPORTED).build());

        leaveEventsUponLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
        verify(userRepository).getForLogin(anyString(), anyString());
        verify(participationService).leave(eq(u));

        verify(response).sendRedirect(eq("UNDERTEST/login?success"));
        verify(response).setStatus(eq(200));
    }
}