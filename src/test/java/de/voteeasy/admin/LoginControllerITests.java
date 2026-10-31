package de.voteeasy.admin;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginControllerITests extends AbstractIT {
    @InjectMocks
    private LoginController loginController;

    @Autowired
    private MockMvc mvc;

    @Test
    void controllerIsCorrectlyWired() {
        assertThat(loginController).isNotNull();
    }

    @Test
    void controllerNeedsAuthentication() throws Exception {
        mvc.perform(get("/app/flashmessage")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void flashmessageIsTransmittedAndSetInTheModel() throws Exception {
        mvc.perform(get("/app/flashmessage")).andExpect(status().isOk()).andExpect(view().name("app/index.html"))
                //.andDo(print())
                .andExpect(model().attribute("flashmessage", equalTo("headers.commands")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void retrieveUsernameViaRepositoryWithoutMocks() throws Exception {
        mvc.perform(get("/app/username")).andExpect(status().isOk())
                .andExpect(content().string(equalToCompressingWhiteSpace("ANONYMOUS")));
    }
}
