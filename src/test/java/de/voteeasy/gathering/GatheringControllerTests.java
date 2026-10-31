package de.voteeasy.gathering;

import de.voteeasy.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GatheringControllerTests extends AbstractIT {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private GatheringService gatheringService;
    @Autowired
    private GatheringController gatheringController;

    @Test
    void controllerIsWiredCorrectly() {
        assertThat(gatheringController).isNotNull();
    }

    @Test
    @WithMockUser(username = "emil")
    void showBasePageWithInjectedUsername() {
        final ModelAndView modelAndView = gatheringController.showAll();

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("gatherings/index");
        // 20211122: fails with SB 2.6.0
        // assertThat(modelAndView.getModel()).hasFieldOrProperty("principal");
        assertThat(modelAndView.getModel().get("gatherings")).isNotNull();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void deleteGatheringWorksForAdminsOnly() {
        final UUID gatheringId = UUID.randomUUID();

        final RedirectView view = gatheringController.deleteGathering(gatheringId);
        assertThat(view.getUrl()).isEqualTo("/gatherings/");
        verify(gatheringService).deleteCascadinglyWithoutTenantCheck(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void newShowsCreationForm() {
        final Gathering g = new Gathering();
        final ModelAndView view = gatheringController.showCreationForm(g);
        assertThat(view.getViewName()).isEqualTo("gatherings/add");
    }

    @Test
    @WithMockUser(username = "user")
    void newShowsCreationFormForAdminsOnly() {
        assertThrows(AccessDeniedException.class, () -> gatheringController.showCreationForm(new Gathering()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void joinGatheringWorksWithUnknownIdPassedIn() throws Exception {
        mvc.perform(get("/gatherings/participate/" + UUID.randomUUID())).andExpect(status().isOk()).andExpect(view().name("gatherings/participate"))
                .andExpect(model().attribute("gathering", is(Optional.empty())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void joinGatheringWorksWithCorrectGatheringIdPassedIn() throws Exception {
        final UUID gatheringId = UUID.randomUUID();
        final Gathering gathering = new Gathering();
        gathering.setName("joinGatheringWorksWithCorrectGatheringIdPassedIn");
        when(gatheringService.getWithTenantCheck(gatheringId)).thenReturn(Optional.of(gathering));

        mvc.perform(get("/gatherings/participate/" + gatheringId)).andExpect(status().isOk()).andExpect(view().name("gatherings/participate"))
                .andExpect(model().attribute("gathering", is(Optional.of(gathering))));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void whenAddGatheringWithMissingRequiredField_addFormWithErrorMessagesIsRendered() throws Exception {
        Gathering g = new Gathering();
        g.setId(UUID.randomUUID());

        mvc.perform(post("/gatherings/add").with(csrf()).content(g.toString()).contentType(MediaType.TEXT_HTML)).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrorCode("gathering", "name", "NotEmpty")).andExpect(view().name("gatherings/add"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void whenGatheringIsAddedTenantAndUserArePersistedAsWell() throws Exception {
        mvc.perform(post("/gatherings/add").with(csrf())
                .param("name", "myGatheringName")
        ).andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/gatherings/"));
        verify(gatheringService).saveWithTenantAndUser(any());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void addGathering() throws Exception {
        mvc.perform(get("/gatherings/new")).andExpect(status().isOk()).andExpect(view().name("gatherings/add"));
    }

    @Test
    @WithMockUser
    void addGatheringWorksForAdminsOnly() throws Exception {
        mvc.perform(get("/gatherings/new")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void postNewGatheringIsForAdminsOnly() throws Exception {
        mvc.perform(post("/gatherings/add").with(csrf())
                .param("name", "myGatheringName")
        ).andExpect(status().isForbidden());
        verifyNoInteractions(gatheringService);
    }
}
