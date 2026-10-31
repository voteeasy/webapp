package de.voteeasy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityITests extends AbstractIT {

    @Autowired
    private MockMvc mvc;

    @WithMockUser(value = "spring")
    @Test
    void givenAnInvalidURL_anErrorPageIsShown() throws Exception {
        mvc.perform(get("/unknown/unknown").contentType(MediaType.TEXT_HTML))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void privateApplicationPartShouldBeProtected() throws Exception {
        mvc.perform(
                get("/gatherings/"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(
            username = "user",
            roles = {"USER", "ADMIN"}
    )
    void logoutRouteIsUnprotectedAndRedirectsToLogin() throws Exception {
        mvc.perform(
                get("/logout"))
                .andExpect(
                        status().is3xxRedirection()
                ).andExpect(redirectedUrl("/login?success"));
    }
}
