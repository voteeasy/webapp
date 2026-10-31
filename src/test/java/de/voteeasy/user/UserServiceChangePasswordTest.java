package de.voteeasy.user;

import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.settings.ChangePasswordForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceChangePasswordTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_NAME = "usr";
    private static final String USER_DISPLAY_NAME = "\uD83D\uDD96usr";

    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void updateChangeUserPasswordWithSecurityCheckSuccessCase() {
        getLoggedInUser();
        ServiceResult result = userService.updateUserWithSecurityCheck(ChangePasswordForm.builder().userId(USER_ID).userName(USER_NAME).fullName(USER_DISPLAY_NAME).password("p").passwordAgain("p").build());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessageKey()).isEqualTo("changepassword.success");

        verify(userRepository).save(any());
    }


    private void getLoggedInUser() {
        User u = new User();
        u.setId(USER_ID);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
    }

    @Test
    public void updateFailsIfNoUserIsLoggedIn() {
        ServiceResult result = userService.updateUserWithSecurityCheck(ChangePasswordForm.builder().userId(USER_ID).userName(USER_NAME).fullName(USER_DISPLAY_NAME).build());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    public void updateFailsIFormDoesNotHaveAUserId() {
        getLoggedInUser();
        ServiceResult result = userService.updateUserWithSecurityCheck(ChangePasswordForm.builder().userName(USER_NAME).fullName(USER_DISPLAY_NAME).build());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    public void updateFailsIFormUserIdDoesNotMatchLoggedInUser() {
        getLoggedInUser();
        ServiceResult result = userService.updateUserWithSecurityCheck(ChangePasswordForm.builder().userId(UUID.randomUUID()).userName(USER_NAME).fullName(USER_DISPLAY_NAME).build());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

}