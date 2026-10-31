package de.voteeasy.votes;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

class VoteServiceTest extends AbstractIT {
    @Autowired
    private VoteService voteService;

    @MockitoBean
    private VoteProposalRepository voteProposalRepository;
    @MockitoBean
    private ApplicationUserService applicationUserService;

    @Test
    void canBeProperlyWired() {
        assertThat(voteService).isNotNull();
    }

    @Test
    void doVoteWithUnknownProposalId() {
        ServiceResult result = voteService.performVote(VoteForm.builder().voteProposalId(UUID.randomUUID()).build());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteProposalWithRandomIdDoesNotThrowAnyException() {
        assertAll(() -> voteService.deleteProposalById(UUID.randomUUID()));
    }

    @Test
    void deleteVoteWithRandomIdReturnsNotFoundResult() {
        final ServiceResult result = voteService.deleteVote(UUID.randomUUID());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("vote.deletion.unknownId");
    }

    @Test
    void deleteNotAllowedForUnknownId() {
        Vote v = new Vote();
        v.setProposal(VoteProposal.builder().stoppedAt(null).build());
        assertThat(voteService.canUserDeleteOwnVote(v)).isFalse();
    }

    @Test
    void startVotingWithRandomIdDoesNotThrowAnyException() {
        assertAll(() -> voteService.startVoting(UUID.randomUUID()));
    }

    @Test
    void stopVotingWithRandomIdDoesNotThrowAnyException() {
        assertAll(() -> voteService.stopVoting(UUID.randomUUID()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCanVoteOnAVoteProposalThatIsOpenForAdmins_emulateOptionLostCase() {
        UUID voteProposalId = UUID.randomUUID();
        VoteProposal adminAllowed = VoteProposal.builder().id(voteProposalId).isAdminAllowedToVote(true).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(adminAllowed));

        Tenant tenant = new Tenant("adminCanVoteOnAVoteProposalThatIsOpenForAdmins_emulateOptionLostCase");
        tenant.setId(UUID.randomUUID());
        User user = User.builder().userName("adminCanVoteOnAVoteProposalThatIsOpenForAdmins_emulateOptionLostCase").id(UUID.randomUUID()).organisation(tenant).build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        VoteForm voteForm = VoteForm.builder().voteProposalId(voteProposalId).selectedOptions(Set.of("A")).build();
        final ServiceResult serviceResult = voteService.performVote(voteForm);
        assertThat(serviceResult).isNotNull();
        // HINT! as we did not have any options selected we run into the #116 error by choice
        assertThat(serviceResult.getStatus()).isEqualTo(BAD_REQUEST);
        assertThat(serviceResult.getMessageKey()).isEqualTo("error.notVoted");
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void adminCannotVoteOnAVoteProposalThatIsNotOpenForAdminsIfUserHasAdminRights() {
        UUID voteProposalId = UUID.randomUUID();
        VoteProposal noAdminAllowed = VoteProposal.builder().id(voteProposalId).isAdminAllowedToVote(false).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(noAdminAllowed));

        User admin = User.builder().userName("adminCannotVoteOnAVoteProposalThatIsNotOpenForAdminsIfUserHasAdminRights").build();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(admin));
        when(applicationUserService.currentlyLoggedInUserHasAtLeastOneOfRoles(eq("ADMIN"))).thenReturn(Optional.of(admin));

        VoteForm voteForm = VoteForm.builder().voteProposalId(voteProposalId).selectedOptions(Set.of("A")).build();
        final ServiceResult serviceResult = voteService.performVote(voteForm);
        assertThat(serviceResult).isNotNull();
        assertThat(serviceResult.getStatus()).isEqualTo(METHOD_NOT_ALLOWED);
        assertThat(serviceResult.getMessageKey()).isEqualTo("proposal.symbol.noadminvote");
    }

}
