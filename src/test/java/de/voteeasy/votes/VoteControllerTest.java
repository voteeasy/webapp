package de.voteeasy.votes;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VoteControllerTest extends AbstractIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VoteRepository voteRepository;
    @MockitoBean
    private VoteService voteService;
    @MockitoBean
    private VoteAggregatorService voteAggregatorService;
    @MockitoBean
    private ApplicationUserService applicationUserService;

    @Autowired
    private VoteController voteController;

    @Test
    void controllerIsWiredCorrectly() {
        assertThat(voteController).isNotNull();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void showAllOfVotingProposalsWithUnknownId() throws Exception {
        mvc.perform(get("/votes/proposal/" + UUID.randomUUID())).andExpect(status().isOk()).andExpect(view().name("votes/show-by-id"))
                .andExpect(model().attribute("votes", empty()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanDeleteVote() throws Exception {
        final UUID voteId = UUID.randomUUID();
        final ServiceResult result = ServiceResult.builder().status(HttpStatus.NOT_FOUND).messageKey("notFoundKey").build();
        when(voteService.deleteVote(eq(voteId))).thenReturn(result);

        mvc.perform(post("/votes/delete/" + voteId).with(csrf())).andExpect(status().isFound()).andExpect(view().name("redirect:/votes/")).andExpect(flash().attribute(VotingController.FLASH_MESSAGE, "notFoundKey"));
    }

    @Test
    @WithMockUser(username = "user")
    void regularUserCannotDeleteVotes() throws Exception {
        mvc.perform(post("/votes/delete/" + UUID.randomUUID()).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void showAnonymizedVotingProposalsRemovesVoters() throws Exception {
        // when anonymized data is retrieved and vote has voter attribute ....
        final UUID voteId = UUID.randomUUID();
        final UUID proposalId = UUID.randomUUID();
        final VoteProposal voteProposal = VoteProposal.builder().id(proposalId).isAnonymized(true).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.of(voteProposal));
        final Vote vote = new Vote();
        vote.setId(voteId);
        vote.setCreatedAt(0L);
        vote.setVoter(new User());
        final Set<Vote> anonymizedVotes = Sets.newLinkedHashSet(vote);
        when(voteRepository.findByProposal(eq(voteProposal))).thenReturn(anonymizedVotes);
        when(voteService.shouldVotesBeShown(any())).thenReturn(true);

        // then voters are taken out of the resulting vote list
        final Vote voteStripped = new Vote();
        voteStripped.setId(voteId);
        voteStripped.setCreatedAt(0L);
        mvc.perform(get("/votes/proposal/" + proposalId))
                // @formatter:off
                .andExpect(status().isOk())
                .andExpect(model().attribute("proposal", voteProposal))
                .andExpect(model().attribute("votes", Sets.newLinkedHashSet(voteStripped)))
                .andExpect(model().attributeExists("voteChartMap"));
        // @formatter:oon
    }

    @Test
    @WithMockUser(username = "user")
    void showVotingProposalsKeepsVoters() throws Exception {
        // when regular data with voter is retrieved ....
        final UUID voteId = UUID.randomUUID();
        final UUID proposalId = UUID.randomUUID();
        final VoteProposal voteProposal = VoteProposal.builder().id(proposalId).isAnonymized(true).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.of(voteProposal));
        final Vote vote = new Vote();
        vote.setId(voteId);
        vote.setCreatedAt(0L);
        vote.setVoter(new User());
        final Set<Vote> anonymizedVotes = Sets.newLinkedHashSet(vote);
        when(voteRepository.findByProposal(eq(voteProposal))).thenReturn(anonymizedVotes);
        when(voteService.shouldVotesBeShown(any())).thenReturn(true);

        // then it is delivered to the frontend/without changes
        mvc.perform(get("/votes/proposal/" + proposalId))
                // @formatter:off
                .andExpect(status().isOk())
                .andExpect(model().attribute("proposal", voteProposal))
                .andExpect(model().attribute("votes", Sets.newLinkedHashSet(vote)))
                .andExpect(model().attributeExists("voteChartMap"));
        // @formatter:oon
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void showAllVotes() throws Exception {
        mvc.perform(get("/votes/")).andExpect(status().isOk()).andExpect(view().name("votes/show"))
                .andExpect(model().attribute("votes", empty()));
    }

}
