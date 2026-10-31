package de.voteeasy.invitation;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.voteeasy.user.UserRegistrationService.KEY_TENANT;
import static de.voteeasy.user.UserRegistrationService.KEY_USERNAME;
import static de.voteeasy.votes.VotingController.FLASH_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserTokenRegistrationControllerTest extends AbstractIT {
    @MockitoBean
    private RegisterByUserTokenService registerByUserTokenService;

    @Autowired
    private MockMvc mvc;

    @InjectMocks
    private UserTokenRegistrationController userTokenRegistrationController;

    @Test
    void controllerIsCorrectlyWired() {
        assertThat(userTokenRegistrationController).isNotNull();
        assertThat(mvc).isNotNull();
    }

    @Test
    void showRegisterForm() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register.html"))
                .andExpect(model().attributeExists("register"));
    }

    @Test
    void processRegisterFormWithFormErrors() throws Exception {
        mvc.perform(post("/register")
                .with(csrf())
                .param("password", "p12345")
                .param("passwordAgain", "p12345")
        ).andExpect(status().isOk())
                .andExpect(view().name("register.html"))
                .andExpect(model().attributeHasFieldErrorCode("register", "token", "NotEmpty"))
                .andExpect(model().attributeExists("register"));
        verifyNoInteractions(registerByUserTokenService);
    }

    @Test
    void processRegisterFormWithErrorInUnderlyingServices() throws Exception {
        when(registerByUserTokenService.redeemToken(any())).thenReturn(ServiceResult.builder().messageKey("flashMe").status(HttpStatus.NOT_ACCEPTABLE).build());

        mvc.perform(post("/register")
                .with(csrf())
                .param("token", "deadbeef")
                .param("password", "p12345")
                .param("passwordAgain", "p12345")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register?error"))
                .andExpect(flash().attribute(FLASH_MESSAGE, "flashMe"))
        ;
        verify(registerByUserTokenService).redeemToken(any());
    }

    @Test
    void processRegisterForm() throws Exception {
        ServiceResult resultWithParams = ServiceResult.builder().messageKey("flashMe").status(HttpStatus.OK).build();
        resultWithParams.add(KEY_TENANT, "NT");
        resultWithParams.add(KEY_USERNAME, "NUSR");
        when(registerByUserTokenService.redeemToken(any())).thenReturn(resultWithParams);

        mvc.perform(post("/register")
                .with(csrf())
                .param("token", "deadbeef")
                .param("password", "p12345")
                .param("passwordAgain", "p12345")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(FLASH_MESSAGE, "flashMe"))
                .andExpect(redirectedUrl("/register?regsuccess&newTenant=NT&newUsername=NUSR"))
        ;
        verify(registerByUserTokenService).redeemToken(any());
    }
}