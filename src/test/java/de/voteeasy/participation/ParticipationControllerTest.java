package de.voteeasy.participation;

import de.voteeasy.AbstractIT;
import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.serviceresult.ServiceResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static de.voteeasy.votes.VotingController.FLASH_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ParticipationControllerTest extends AbstractIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ParticipationService participationService;
    @MockitoBean
    private GatheringService gatheringService;

    @Autowired
    private ParticipationController participationController;

    @Test
    void wiringWorksFine() {
        assertThat(participationController).isNotNull();
    }

    @Test
    @WithMockUser(username = "user")
    void getParticipantsAsNonAdmin() throws Exception {
        mvc.perform(get("/participation/members").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getParticipantsAsAdmin() throws Exception {
        mvc.perform(get("/participation/members").with(csrf()))
                .andExpect(view().name("participation/participants"))
                .andExpect(model().attributeExists("participants"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getParticipantsAsAdminWithData() throws Exception {
        Participant participant = Participant.builder().fullName("Wladimir Wladimirowitsch").gathering("RockThePops").userName("usr").build();
        when(participationService.getParticipants()).thenReturn(Set.of(participant));

        mvc.perform(get("/participation/members").with(csrf()))
                .andExpect(view().name("participation/participants"))
                .andExpect(model().attribute("participants", hasItem(participant)))
                .andExpect(model().attribute("participants", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void showAllForUser() throws Exception {
        Participation participation = new Participation();
        participation.setId(UUID.randomUUID());
        Gathering gathering = new Gathering();
        gathering.setName("showAllForUser");
        participation.setEvent(gathering);
        when(participationService.getAllParticipations()).thenReturn(Set.of(participation));

        mvc.perform(get("/participation/").with(csrf()))
                .andExpect(view().name("participation/index"))
                .andExpect(model().attribute("participations", Set.of(participation)))
                .andExpect(status().isOk());
    }

    @Test
    void showAllForUserNeedsAuthentication() throws Exception {
        mvc.perform(get("/participation/").with(csrf()))
                .andExpect(redirectedUrl("http://localhost/login"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user")
    void takePartSuccess() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        ServiceResult participateResult = ServiceResult.builder().status(OK).messageKey("i18n.takePartSuccess").build();
        when(participationService.participateIn(any())).thenReturn(participateResult);

        mvc.perform(post("/participation/participate/" + gatheringId).with(csrf()))
                .andExpect(redirectedUrl("/gatherings/participate/" + gatheringId))
                .andExpect(flash().attribute(FLASH_MESSAGE, "i18n.takePartSuccess"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user")
    void takePartNotOk() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        ServiceResult participateResult = ServiceResult.builder().status(NOT_ACCEPTABLE).messageKey("i18n.takePartNotOk").build();
        when(participationService.participateIn(any())).thenReturn(participateResult);

        mvc.perform(post("/participation/participate/" + gatheringId).with(csrf()))
                .andExpect(redirectedUrl("/gatherings/"))
                .andExpect(flash().attribute(FLASH_MESSAGE, "i18n.takePartNotOk"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user")
    void leaveWithoutAnythingToLeave() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        ServiceResult leaveResult = ServiceResult.builder().status(NOT_IMPLEMENTED).messageKey("i18n.leaveWithoutAnythingToLeave").build();
        when(participationService.leave()).thenReturn(leaveResult);

        mvc.perform(post("/participation/leave/" + gatheringId).with(csrf()))
                .andExpect(redirectedUrl("/gatherings/participate/" + gatheringId))
                .andExpect(flash().attribute(FLASH_MESSAGE, "i18n.leaveWithoutAnythingToLeave"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user")
    void leaveSuccessfully() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        ServiceResult leaveResult = ServiceResult.builder().status(OK).messageKey("i18n.leaveSuccessfully").build();
        when(participationService.leave()).thenReturn(leaveResult);

        mvc.perform(post("/participation/leave/" + gatheringId).with(csrf()))
                .andExpect(redirectedUrl("/gatherings/"))
                .andExpect(flash().attribute(FLASH_MESSAGE, "i18n.leaveSuccessfully"))
                .andExpect(status().is3xxRedirection());
    }
}