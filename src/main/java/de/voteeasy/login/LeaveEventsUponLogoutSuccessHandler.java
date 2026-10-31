package de.voteeasy.login;

import de.voteeasy.participation.ParticipationService;
import de.voteeasy.user.ApplicationUser;
import de.voteeasy.user.User;
import de.voteeasy.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class LeaveEventsUponLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ParticipationService participationService;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private UserRepository userRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (authentication != null && authentication.getPrincipal() instanceof ApplicationUser userDetails) {

            Optional<User> user = userRepository.getForLogin(userDetails.getUsername(), userDetails.getTenant().getTenantName());
            user.ifPresent(u -> log.info("User {} has been logged out {}.", u.getId(), participationService.leave(u).getStatus().name()));
        } else {
            log.info("Unable to perform proper logout due to wrong class of UserCredentials, this is okay during test runs.");
        }

        // redirect to loginSuccessUrl :)
        String URL = request.getContextPath() + "/login?success";
        response.setStatus(HttpStatus.OK.value());
        response.sendRedirect(URL);

        super.onLogoutSuccess(request, response, authentication);
    }

}
