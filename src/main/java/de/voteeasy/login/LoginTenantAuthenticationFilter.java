package de.voteeasy.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
public class LoginTenantAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    public static final String FORM_FIELD_TENANT = "organisation";
    private static final String ORDER_OF_PARAMS = "%s%s%s";
    private static final String DELIMITER = String.valueOf(Character.LINE_SEPARATOR);

    // for testing and consistent usage
    public static String toUserNameWithTenant(@Nullable String userName, @Nullable String tenantName) {
        if (userName == null) {
            userName = "";
        }
        if (tenantName == null) {
            tenantName = "";
        }

        return String.format(ORDER_OF_PARAMS, userName.trim().toLowerCase(), DELIMITER, tenantName.trim().toLowerCase());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!RequestMethod.POST.name().equals(request.getMethod())) {
            log.error("Invalid authentication method invoked, was: {}", request);
            throw new AuthenticationServiceException("Unsupported authentication method used: " + request.getMethod());
        }

        UsernamePasswordAuthenticationToken authRequest = extractAuthParametersFromRequest(request);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private UsernamePasswordAuthenticationToken extractAuthParametersFromRequest(HttpServletRequest request) {
        String password = obtainPassword(request);
        if (password == null) {
            password = "";
        }

        return new UsernamePasswordAuthenticationToken(toUserNameWithTenant(obtainUsername(request), obtainTenant(request)), password);
    }

    private String obtainTenant(HttpServletRequest request) {
        return request.getParameter(FORM_FIELD_TENANT);
    }
}