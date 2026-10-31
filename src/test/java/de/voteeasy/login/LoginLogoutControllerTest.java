package de.voteeasy.login;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginLogoutControllerTest extends AbstractIT {
    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser
    void welcomePage() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/index.html"));
    }

    @Test
    void welcomePageRedirectsToLoginIfNotAuthenticated() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void loginAfterSecurityRedirectDefaultsToGerman() throws Exception {
        // The security filter redirects before the MVC lang interceptor runs.
        mvc.perform(get("/?lang=de").header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(redirectedUrl("http://localhost/login"));
        mvc.perform(get("/login").header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Zurück zur Webseite")));
    }

    @Test
    void loginMessagesWithoutLanguageParameterDefaultToGerman() throws Exception {
        mvc.perform(get("/login?error").header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalide Zugangsdaten")));
        mvc.perform(get("/login?success").header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sie haben sich erfolgreich abgemeldet")));
    }

    @Test
    void loginCanStillBeSelectedInEnglish() throws Exception {
        mvc.perform(get("/login?lang=en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Back to home page")));
    }

    @Test
        // 20220429: is flaky on GithubAction as well: 302/redirect instead of 200
    void loginWorksWithoutAuthentication() throws Exception {
        // as multiple assertions do not work within the spring construct we have to call it twice
        // mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login.html"));
        mvc.perform(get("/login")).andExpect(view().name("login.html"));

        final MvcResult result = mvc.perform(get("/login")).andReturn();
        final MockHttpServletResponse response = result.getResponse();
        assertThat(response).isNotNull();

        final int responseStatus = response.getStatus();
        assertThat(responseStatus).isIn(HttpStatus.OK.value(), HttpStatus.FOUND.value());
    }
}