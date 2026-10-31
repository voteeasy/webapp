package de.voteeasy.admin;

import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {
    public static final String UNDER_TEST = "UnderTest";
    @Mock
    private ApplicationUserService applicationUserService;

    @InjectMocks
    private LoginController loginController;

    @Test
    void controllerIsCorrectlyWired() {
        assertThat(loginController).isNotNull();
    }

    @Test
    void retrieveUsernameViaRepositoryWithMocks() {
        User u = new User();
        u.setUserName(UNDER_TEST);
        given(applicationUserService.currentlyLoggedInUser()).willReturn(Optional.of(u));

        String userName = loginController.getUserName(UNDER_TEST, null);
        assertThat(userName).isEqualTo(UNDER_TEST + "/class de.voteeasy.user.User");
    }

    @Test
    void errorMessageCreatorOkay() {
        final ResponseEntity<Object> responseEntity = loginController.handleCall(200);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }

    @Test
    void errorMessageCreatorInternalServerError() {
        final ResponseEntity<Object> responseEntity = loginController.handleCall(500);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    void errorMessageCreatorNotFound() {
        final ResponseEntity<Object> responseEntity = loginController.handleCall(404);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void errorMessageCreatorForbidden() {
        final ResponseEntity<Object> responseEntity = loginController.handleCall(403);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void errorMessageCreatorDefaultCase() {
        final ResponseEntity<Object> responseEntity = loginController.handleCall(4711);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    }
}
