package de.voteeasy.votes;

import de.voteeasy.AbstractIT;
import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.participation.Participation;
import de.voteeasy.participation.ParticipationService;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VotingControllerTest extends AbstractIT {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GatheringService gatheringService;
    @MockitoBean
    private VotingService votingService;
    @MockitoBean
    private VoteService voteService;
    @MockitoBean
    private ParticipationService participationService;

    @Autowired
    private VotingController votingController;

    @Test
    void controllerIsWiredCorrectly() {
        assertThat(votingController).isNotNull();
    }

    @Test
    @WithMockUser(username = "user")
    void regularUserCannotDeleteProposals() throws Exception {
        mvc.perform(post("/votings/proposals/delete/" + UUID.randomUUID()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanDeleteVoteProposal() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final VoteProposal voteProposal = VoteProposal.builder().id(proposalId).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.of(voteProposal));
        doNothing().when(voteService).deleteProposalById(any());

        mvc.perform(post("/votings/proposals/delete/" + proposalId)
                // @formatter:off
                .with(csrf())).andExpect(status().isFound())
                .andExpect(redirectedUrl("/gatherings/"))
                // @formatter:on
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, equalTo("success.proposalDeletion")));
        verify(voteService).deleteProposalById(eq(proposalId));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanSaveVoteProposalAndWillGetErrorResultsShownInForm() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering("mGathering", new Tenant());
        when(gatheringService.getWithTenantCheck(any())).thenReturn(Optional.of(gathering));

        VoteProposalForm form = VoteProposalForm.builder().name("name").options(List.of("myOption")).gatheringName("myGathering").gatheringId(gatheringId).build();
        mvc.perform(post("/votings/proposals/addproposal")
                        // @formatter:off
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("proposal", form)
                // @formatter:on
        )
                // .andDo(print())
                .andExpect(status().is2xxSuccessful()).andExpect(view().name("votings/proposal-add"));
    }

    @Test
    @WithMockUser(username = "tenantadmin", roles = {"USER", "ADMIN"})
    void adminCanRetrieveVotingProposalForm() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering("mGathering", new Tenant());
        gathering.setId(gatheringId);
        when(gatheringService.getWithTenantCheck(any())).thenReturn(Optional.of(gathering));

        mvc.perform(get("/votings/proposals/add/" + gatheringId)
                        // @formatter:off
                        .with(csrf())
                // @formatter:on
        )
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("votings/proposal-add"))
                .andExpect(model().attribute("gatheringId", gatheringId))
                .andExpect(model().attribute("gatheringName", "mGathering"))
                .andExpect(model().attributeExists("proposal"))
        ;
    }

    @Test
    @WithMockUser(username = "tenantadmin", roles = {"USER", "ADMIN"})
    void votingProposalCannotBeCreatedWithUnknownGatheringIdGiven() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        when(gatheringService.getWithTenantCheck(any())).thenReturn(Optional.empty());

        mvc.perform(get("/votings/proposals/add/" + gatheringId)
                        // @formatter:off
                        .with(csrf())
                // @formatter:on
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/gatherings/"))
        ;
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanSaveVoteProposal() throws Exception {
        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering("mGathering", new Tenant());
        when(gatheringService.getWithTenantCheck(any())).thenReturn(Optional.of(gathering));

        ServiceResult result = ServiceResult.builder().status(HttpStatus.ALREADY_REPORTED).messageKey("myKey").build();
        when(votingService.addProposal(any())).thenReturn(result);

        mvc.perform(post("/votings/proposals/addproposal")
                .with(csrf())
                .param("options", Sets.newLinkedHashSet("optionByParam").toString())
                .param("name", "nameByParam")
                .param("gatheringId", gatheringId.toString())
        )
                // .andDo(print())
                .andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/gatherings/participate/" + gatheringId));
    }

    @Test
    @WithMockUser(username = "emil")
    void userCanNotRequestCreateVoteProposalForm() throws Exception {
        mvc.perform(get("/votings/proposals/add/" + UUID.randomUUID()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "emil")
    void userCanNotPersistNewVoteProposalForm() throws Exception {
        VoteProposalForm form = VoteProposalForm.builder().name("name").options(List.of("option")).build();
        mvc.perform(post("/votings/proposals/addproposal")
                // @formatter:off
                .with(csrf())
                .content(form.toString()).contentType(MediaType.TEXT_HTML)
                // @formatter:on
                .param("proposal", form.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "emil")
    void makeSureUnknownIdDoesNotThrowExceptionWhenRetrievingVoteProposals() throws Exception {
        mvc.perform(get("/votings/proposals/" + UUID.randomUUID()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "emil")
    void voteChecksIfParticipationExistsAndShowsFlashMessageIfMissing() throws Exception {
        UUID proposalId = UUID.randomUUID();

        VoteProposal voteProposal = VoteProposal.builder().id(proposalId).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.of(voteProposal));

        mvc.perform(get("/votings/vote/" + proposalId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gatherings/"))
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "votes.need.participation"))
        ;
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanStartVotingAndWillGetFlashMessagesAndRedirectToGatheringOverview() throws Exception {
        mvc.perform(get("/votings/proposals/startVoting/" + UUID.randomUUID())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/gatherings/")).andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success.votingStarted"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanStopVotingAndWillGetFlashMessagesAndRedirectToVotingProposalsList() throws Exception {
        mvc.perform(get("/votings/proposals/endVoting/" + UUID.randomUUID())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/gatherings/")).andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "success.votingFinished"));
    }

    @Test
    @WithMockUser(username = "user")
    void startAndEndVotingForAdminsOnly() throws Exception {
        mvc.perform(get("/votings/proposals/startVoting/" + UUID.randomUUID())).andExpect(status().isForbidden());
        mvc.perform(get("/votings/proposals/endVoting/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void performVoteWithParticipation() throws Exception {
        UUID voteProposalId = UUID.randomUUID();
        VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).selectedOptions(Set.of("Y")).availableOptions(Set.of("Y", "N")).build();

        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering();
        gathering.setId(gatheringId);
        VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).name("performVote").gathering(gathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        Participation participation = new Participation();
        participation.setEvent(gathering);
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));

        ServiceResult voteResult = ServiceResult.builder().messageKey("performVote.i18n").status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).build();
        when(voteService.performVote(eq(form))).thenReturn(voteResult);

        mvc.perform(post("/votings/vote/dovote").with(csrf())
                .flashAttr("vote", form) // that's the way to encode a modelAttribute
        ).andDo(print())
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/votings/proposals/" + gatheringId))
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "performVote.i18n"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void performVoteWithoutParticipation() throws Exception {
        // TODO #44
        UUID voteProposalId = UUID.randomUUID();
        VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).selectedOptions(Set.of("Y")).availableOptions(Set.of("Y", "N")).build();

        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering();
        gathering.setId(gatheringId);
        VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).name("performVote").gathering(gathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        mvc.perform(post("/votings/vote/dovote").with(csrf())
                .flashAttr("vote", form) // that's the way to encode a modelAttribute
        ).andDo(print())
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/gatherings/"))
                .andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "votes.need.participation"));
    }

}
