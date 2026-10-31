package de.voteeasy.selfservice;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SelfServiceRegistrationControllerTest extends AbstractIT {

    @Autowired
    private SelfServiceRegistrationController controller;

    @Autowired
    private MockMvc mvc;

    @Test
    void wiringWorkedCorrectly() {
        assertThat(controller).isNotNull();
    }

    @Test
    void showSelfRegisterForm() throws Exception {
        mvc.perform(get("/registerselfservice"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("register"))
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("registerselfservice.html"));
    }

    @Test
    void postEmptySelfRegisterFormYieldsError() throws Exception {
        mvc.perform(post("/registerselfservice"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("register", "captcha", "NotEmpty"))
                .andExpect(view().name("registerselfservice.html"));
    }

}