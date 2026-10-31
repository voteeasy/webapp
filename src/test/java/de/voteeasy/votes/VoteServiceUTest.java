package de.voteeasy.votes;


import de.voteeasy.gathering.Gathering;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.tenant.Tenant;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceUTest {
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VoteProposalRepository voteProposalRepository;
    // is used from within VoteService - do not delete
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private ApplicationUserService applicationUserService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private VoteService voteService;

    // Collaborators
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @Test
    void deleteVoteOkayForKnownVoteId() {
        UUID voteId = UUID.randomUUID();
        Vote v = new Vote();
        v.setId(voteId);
        when(voteRepository.findById(eq(voteId))).thenReturn(Optional.of(v));

        VoteProposal voteProposal = new VoteProposal();
        v.setProposal(voteProposal);

        User user = new User();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));
        v.setVoter(user);

        ServiceResult result = voteService.deleteVote(voteId);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessageKey()).isEqualTo("vote.deletion.success");
    }

    @Test
    void deleteVoteForbiddenForKnownVoteIdButMissingProposalAndVoterMismatch() {
        UUID voteId = UUID.randomUUID();
        Vote v = new Vote();
        v.setId(voteId);
        when(voteRepository.findById(eq(voteId))).thenReturn(Optional.of(v));

        ServiceResult result = voteService.deleteVote(voteId);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result.getMessageKey()).isEqualTo("vote.deletion.forbidden");
    }

    @Test
    void voteIfVotingIsStoppedAlreadyReturnsStatusForbidden() {
        final UUID voteProposalId = UUID.randomUUID();
        final VoteForm form = new VoteForm();
        form.setVoteProposalId(voteProposalId);

        final VoteProposal voteProposal = VoteProposal.builder().stoppedAt(0L).id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        ServiceResult result = voteService.performVote(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result.getMessageKey()).isEqualTo("error.closedAlready");
    }

    @Test
    void performVotingWithUnknownUserForValidVoteProposalReturnsStatusUnknown() {
        final UUID voteProposalId = UUID.randomUUID();
        final VoteForm form = new VoteForm();
        form.setVoteProposalId(voteProposalId);

        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        ServiceResult result = voteService.performVote(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessageKey()).isEqualTo("error.unknownId");
    }

    @Test
    void performVotingPositiveCaseReturnsStatusOkay() {
        final Option o = new Option("justATest");
        final UUID voteProposalId = UUID.randomUUID();
        final VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).availableOptions(Set.of(o.getName())).selectedOptions(Set.of(o.getName())).build();

        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        when(optionRepository.findByNameInAndVoteProposalEquals(any(), any(VoteProposal.class), any(Sort.class))).thenReturn(Set.of(o));

        setDummyUserAsCurrentlyLoggedIn();
        final Vote savedVote = new Vote();
        savedVote.setChosenOptions(Set.of(o));
        savedVote.setId(UUID.randomUUID());
        when(voteRepository.save(any())).thenReturn(savedVote);


        ServiceResult result = voteService.performVote(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(result.getMessageKey()).isEqualTo("success.voted");
    }

    @Test
    void performVotingOptionsAreNotExistingYieldsStatusBadRequest() {
        final UUID voteProposalId = UUID.randomUUID();
        final VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).build();

        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        setDummyUserAsCurrentlyLoggedIn();

        ServiceResult result = voteService.performVote(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessageKey()).isEqualTo("error.notVoted");
        verify(voteRepository, never()).save(any());
    }

    @Test
    void performVotingAndPreventDoubleVotingResultsInStatusLocked() {
        final UUID voteProposalId = UUID.randomUUID();
        final VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).build();

        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        setDummyUserAsCurrentlyLoggedIn();
        final Vote alreadyVoted = new Vote();
        when(voteRepository.findByVoterAndProposal(any(), any())).thenReturn(Optional.of(alreadyVoted));

        ServiceResult result = voteService.performVote(form);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.LOCKED);
        assertThat(result.getMessageKey()).isEqualTo("error.noDoubleVotingAllowed");
    }

    @Test
    void performVoteAndTryToSubmitAndInvalidVoteWithoutAnyOptionsSelected() {
        final UUID voteProposalId = UUID.randomUUID();
        final VoteForm invalidForm = VoteForm.builder().selectedOptions(null).voteProposalId(voteProposalId).build();

        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        ServiceResult result = voteService.performVote(invalidForm);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessageKey()).isEqualTo("NotEmpty.option.options");
    }

    private User setDummyUserAsCurrentlyLoggedIn() {
        return setDummyUserAsCurrentlyLoggedIn(new Tenant());
    }

    private User setDummyUserAsCurrentlyLoggedIn(@NotNull Tenant t) {
        final User user = new User();
        user.setOrganisation(t);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void deleteVoteProposalByIdWithKnownIdsRemovesDependingStuffFromDatabase() {
        final UUID voteProposalId = UUID.randomUUID();
        final Tenant t = new Tenant();

        final Gathering gathering = new Gathering("Gathering", t);
        final VoteProposal voteProposalForDeletion = VoteProposal.builder().id(voteProposalId).gathering(gathering).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposalForDeletion));

        final User user = new User();
        user.setOrganisation(t);
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));

        voteService.deleteProposalById(voteProposalId);

        // verify cascading deletes
        verify(voteProposalRepository).findById(eq(voteProposalId));
        verify(voteProposalRepository).deleteById(eq(voteProposalId));
        verify(voteRepository).deleteAllByProposal(eq(voteProposalForDeletion));
        // and flushing of EM
        //verify(entityManager).flush();
        //verify(entityManager).clear();
    }

    @Test
    void getVoteFromUserWithUnknownIdIsEmpty() {
        Assertions.assertThat(voteService.getVoteFromUser(UUID.randomUUID())).isEmpty();
    }

    @Test
    void getVoteFromUserWithValidIdIsNotEmpty() {
        UUID voteProposalId = UUID.randomUUID();
        setDummyUserAsCurrentlyLoggedIn();

        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));
        final Vote vote = new Vote();
        when(voteRepository.findByVoterAndProposal(any(), any())).thenReturn(Optional.of(vote));

        final Optional<Vote> getVote = voteService.getVoteFromUser(voteProposalId);
        Assertions.assertThat(getVote).isPresent().contains(vote);
    }

    @Test
    void startVotingResultsInStartTimeOnVoteProposal() {
        UUID voteProposalId = UUID.randomUUID();
        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).build();
        assertThat(voteProposal.getStartedAt()).isNull();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));
        setDummyUserAsCurrentlyLoggedIn();

        voteService.startVoting(voteProposalId);
        assertThat(voteProposal.getStartedAt()).isNotNull();
        verify(voteProposalRepository).save(any());
    }

    @Test
    void stopVotingResultsInStartTimeOnVoteProposal() {
        UUID voteProposalId = UUID.randomUUID();
        final VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).startedAt(0L).build();
        assertThat(voteProposal.getStoppedAt()).isNull();
        when(voteProposalRepository.findById(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));
        setDummyUserAsCurrentlyLoggedIn();

        voteService.stopVoting(voteProposalId);
        assertThat(voteProposal.getStoppedAt()).isNotNull();
        verify(voteProposalRepository).save(any());
    }

    @Test
    void canUserDeleteTestNullArgument() {
        assertThat(voteService.canUserDeleteOwnVote(null)).isFalse();
    }

    @Test
    void canUserDeleteTestNoVoteProposal() {
        assertThat(voteService.canUserDeleteOwnVote(new Vote())).isFalse();
    }

    @Test
    void canUserDeleteTestNoVoter() {
        Vote v = new Vote();
        v.setProposal(new VoteProposal());
        assertThat(voteService.canUserDeleteOwnVote(v)).isFalse();
    }

    @Test
    void canUserDeleteTestVoteProposalWithoutStoppedAt() {
        Vote v = new Vote();
        VoteProposal voteProposal = new VoteProposal();
        v.setProposal(voteProposal);
        assertThat(voteService.canUserDeleteOwnVote(v)).isFalse();
    }

    @Test
    void canUserDeleteTestVoteProposalWithStoppedAtAndWrongUser() {
        Vote v = new Vote();
        VoteProposal voteProposal = new VoteProposal();
        v.setProposal(voteProposal);
        User differentUser = new User();
        differentUser.setUserName("DifferentUser");
        v.setVoter(differentUser);

        setDummyUserAsCurrentlyLoggedIn();

        assertThat(voteService.canUserDeleteOwnVote(v)).isFalse();
    }

    @Test
    void canUserDeleteTestVoteProposalWithStoppedAtAndCorrectUserShouldBeTrue() {
        Vote v = new Vote();
        VoteProposal voteProposal = new VoteProposal();
        v.setProposal(voteProposal);
        v.setVoter(setDummyUserAsCurrentlyLoggedIn());

        assertThat(voteService.canUserDeleteOwnVote(v)).isTrue();
    }

    @Test
    void noVoteProposalNoVotesShown() {
        assertThat(voteService.shouldVotesBeShown(Optional.empty())).isFalse();
    }

    @Test
    void noAdminNotVotesShown() {
        User user = new User();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertThat(voteService.shouldVotesBeShown(Optional.of(VoteProposal.builder().build()))).isFalse();
    }

    @Test
    void ifProposalIsStoppedThenShowVotes() {
        assertThat(voteService.shouldVotesBeShown(Optional.of(VoteProposal.builder().stoppedAt(0L).build()))).isTrue();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    void ifAdminVotesAreShownForIncompleteVoteProposalsAsWell() {
        // tricky mocking due to WildcardGenerics in return type of getAuthorities()
        doReturn(getAdminAuthorities()).when(authentication).getAuthorities();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThat(voteService.shouldVotesBeShown(Optional.of(VoteProposal.builder().build()))).isTrue();
    }

    private Collection<GrantedAuthority> getAdminAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return grantedAuthorities;
    }

}
