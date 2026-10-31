package de.voteeasy.settings;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.UserService;
import de.voteeasy.votes.VotingController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserSettingsControllerTest extends AbstractIT {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private UserSettingsController userSettingsController;

    @Test
    void controllerIsWiredCorrectly() {
        assertThat(userSettingsController).isNotNull();
    }

    /*
     * Problem as ApplicationUserService's result cannot be mocked here!
     * Using a test slice does not work.
     */
    @Test
    @WithMockCustomUser
    void showProperties_notWorkingInTestAsWeCannotMockOurOwnUserObjects() throws Exception {
        mvc.perform(
                get("/settings/changeName"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("/"));
    }

    @Test
    void showPropertiesNeedsAuthentication() throws Exception {
        mvc.perform(
                get("/settings/changeName"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    void postChangeNameWorks() throws Exception {
        var result = ServiceResult.builder().status(HttpStatus.OK).messageKey("works").build();
        when(userService.updateUserWithSecurityCheck(any(ChangeNameForm.class))).thenReturn(result);

        mvc.perform(post("/settings/changeName")
                .with(csrf())
                .param("userId", UUID.randomUUID().toString())
                .param("userName", "undertest")
                .param("fullName", "new display name")
        ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "works"));
    }

    @Test
    @WithMockUser
    void postChangeNameValidatesGivenInput() throws Exception {
        mvc.perform(post("/settings/changeName").with(csrf())).andExpect(status().isOk()).andExpect(view().name("/settings/changeName"));
    }

    @Test
    @WithMockUser
    void postChangePasswordWithNotMatchingPasswordsYieldsGlobalFormError() throws Exception {
        mvc.perform(post("/settings/changePassword").with(csrf())
                .param("userId", UUID.randomUUID().toString())
                .param("userName", "userNameChangePW")
                .param("fullName", "fullNameChangePW")
                .param("password", "really?")
                .param("passwordAgain", "notTheSAME")
        )
                .andExpect(status().isOk())
                // is a global validation error that is not checkable on the model
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("/settings/changePassword"));
    }

    @Test
    @WithMockUser
    void postChangePasswordWithNoPasswordsAtAllYieldsFormError() throws Exception {
        mvc.perform(post("/settings/changePassword").with(csrf())
                .param("userId", UUID.randomUUID().toString())
                .param("userName", "userNameChangePW")
                .param("fullName", "fullNameChangePW")
        )
                .andExpect(status().isOk())
                // is a global validation error that is not checkable on the model
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("account", "password", "passwordAgain"))
                .andExpect(view().name("/settings/changePassword"));
    }

    @Test
    @WithMockUser
    void postChangePasswordWorksIfPasswordsAreEquals() throws Exception {
        var result = ServiceResult.builder().status(HttpStatus.OK).messageKey("pwChangeWorks").build();
        when(userService.updateUserWithSecurityCheck(any(ChangePasswordForm.class))).thenReturn(result);

        mvc.perform(post("/settings/changePassword").with(csrf())
                .param("userId", UUID.randomUUID().toString())
                .param("userName", "userNameChangePW")
                .param("fullName", "fullNameChangePW")
                .param("password", "really?")
                .param("passwordAgain", "really?")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeDoesNotExist("account"))
                .andExpect(model().hasNoErrors())
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "pwChangeWorks"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void changePasswordNeedsAuthentication() throws Exception {
        mvc.perform(
                get("/settings/changePassword"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("http://localhost/login"));
    }
}