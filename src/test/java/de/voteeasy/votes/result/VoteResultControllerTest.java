package de.voteeasy.votes.result;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VoteResultControllerTest extends AbstractIT {
    @Autowired
    private MockMvc mvc;

//    @MockBean
//    private ApplicationUserService applicationUserService;

    @Autowired
    private VoteResultController voteResultController;

    @Test
    void controllerIsWiredCorrectly() {
        assertThat(voteResultController).isNotNull();
    }

    @Test
    void showResultsForGatheringNeedsAuthentication() throws Exception {
        mvc.perform(get("http://localhost/results/" + UUID.randomUUID()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "regularuser")
    void showResultsForGathering() throws Exception {
        mvc.perform(get("http://localhost/results/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(view().name("results/index.html"))
                .andExpect(model().attributeExists("voteResult"))
        ;
    }

}