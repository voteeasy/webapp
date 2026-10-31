package de.voteeasy;

import de.voteeasy.participation.Participation;
import de.voteeasy.participation.ParticipationService;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAndParticipationControllerAdviceTest {
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private ParticipationService participationService;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private Model model;

    @InjectMocks
    private UserAndParticipationControllerAdvice userAndParticipationControllerAdvice;

    @Test
    void wiringWorks() {
        assertThat(userAndParticipationControllerAdvice).isNotNull();
    }

    @Test
    void handleRequestWithoutUserLoggedIn() {
        userAndParticipationControllerAdvice.handleRequest(model);
        verify(model).addAttribute(eq("servletPath"), eq(null));
    }

    @Test
    void handleRequestWithUserLoggedInAndParticipation() {
        Participation participation = new Participation();
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));

        userAndParticipationControllerAdvice.handleRequest(model);
        verify(model).addAttribute(eq("participation"), eq(participation));
    }

    @Test
    void handleRequestWithUserLoggedInAndUserDetailsInformation() {
        User user = User.builder().build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        userAndParticipationControllerAdvice.handleRequest(model);
        verify(model).addAttribute(eq("user"), eq(user));
    }

    @Test
    void verifyHttpServletRequestIsProperlyWiredIn() {
        when(servletRequest.getServletPath()).thenReturn("notARealPath");

        userAndParticipationControllerAdvice.handleRequest(model);
        verify(model).addAttribute(eq("servletPath"), eq("notARealPath"));
    }

}