package de.voteeasy.settings;

import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import de.voteeasy.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsControllerUTest {
    @Mock
    private Model model;
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private UserService userService;

    @InjectMocks
    private UserSettingsController userSettingsController;

    @Test
    void wiringWorkedCorrectly() {
        assertThat(userSettingsController).isNotNull();
    }

    @Test
    void changePassword() {
        UUID userId = UUID.randomUUID();
        User u = loginAsUser(userId);

        final ChangePasswordForm form = ChangePasswordForm.builder().userId(u.getId()).fullName(u.getFullName()).userName(u.getUserName()).build();

        final var result = userSettingsController.changePassword(model);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("/settings/changePassword.html");
        verify(model).addAttribute(eq("account"), eq(form));
    }

    @Test
    void changePasswordWithoutBeingLoggedInRedirectsToWelcomePage() {
        final var result = userSettingsController.changePassword(model);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/");
    }

    private User loginAsUser(UUID userId) {
        User u = new User("userName", "fullName", "pw", new Tenant());
        u.setId(userId);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(u));
        return u;
    }

    @Test
    void changeNameFormIsPopulated() {
        UUID userId = UUID.randomUUID();
        User u = loginAsUser(userId);

        final ChangeNameForm form = ChangeNameForm.builder().userId(u.getId()).fullName(u.getFullName()).userName(u.getUserName()).build();

        final var result = userSettingsController.showChangeNameForm(model);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("/settings/changeName.html");
        verify(model).addAttribute(eq("account"), eq(form));
    }

    @Test
    void changeNameFormHandlesMissingUserAndRedirectsToWelcomepage() {
        final var result = userSettingsController.showChangeNameForm(model);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/");
    }

}
