package de.voteeasy.selfservice;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QrCodeControllerTest extends AbstractIT {

    @Autowired
    private QrCodeController controller;

    @Autowired
    private MockMvc mvc;

    @Test
    void generateQrCodeWithExternalParametersAndDefaultColors() throws Exception {
        var result = mvc.perform(get("/qr/yourtenant/youruser/yourpassword"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andReturn();

        assertThat(result).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotNull();
    }

    @Test
    void generateQrCodeWithExternalParametersIncludingColors() throws Exception {
        var result = mvc.perform(get("/qr/yourtenant/youruser/yourpassword/green/red"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andReturn();

        assertThat(result).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotNull();
    }
}