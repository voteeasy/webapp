package de.voteeasy.login;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginTenantAuthenticationFilterTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private LoginTenantAuthenticationFilter filter;

    @Test
    void objectCreatable() {
        assertThat(filter).isNotNull();
    }

    @Test
    void nullsafeArgs() {
        assertThat(LoginTenantAuthenticationFilter.toUserNameWithTenant(null, null)).isEqualTo("13");
    }

    @Test
    void nullsafeForArgUser() {
        assertThat(LoginTenantAuthenticationFilter.toUserNameWithTenant("usr", null)).isEqualTo("usr13");
    }

    @Test
    void nullsafeForArgTenant() {
        assertThat(LoginTenantAuthenticationFilter.toUserNameWithTenant(null, "tenant")).isEqualTo("13tenant");
    }

    @Test
    void showHowTenantAndUsernameAreEncodedAndLowercased() {
        assertThat(LoginTenantAuthenticationFilter.toUserNameWithTenant("usrNA", "tenanT")).isEqualTo("usrna13tenant");
    }

    @Test
    void errorIfNotPostDuringLogin() {
        when(request.getMethod()).thenReturn("UNKNOWN");
        assertThrows(AuthenticationServiceException.class, () -> filter.attemptAuthentication(request, response));
    }

    @Test
    void extractAllLoginFieldsProperlyByObjectInvocationsOnly() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter(eq("password"))).thenReturn("noCleartextpasswords");

        assertThat(filter.attemptAuthentication(request, response)).isNull();

        verify(authenticationManager).authenticate(any());
        verify(request).getParameter(eq("username"));
        verify(request).getParameter(eq("password"));
        verify(request).getParameter(eq("organisation"));
    }

    @Test
    void nullsafePasswordExtraction() {
        when(request.getMethod()).thenReturn("POST");

        assertThat(filter.attemptAuthentication(request, response)).isNull();
        verify(request).getParameter(eq("password"));
        verify(request).getParameter(eq("organisation"));
    }

}