package de.voteeasy.votes;

import de.voteeasy.gathering.Gathering;
import de.voteeasy.gathering.GatheringService;
import de.voteeasy.participation.Participation;
import de.voteeasy.participation.ParticipationService;
import de.voteeasy.serviceresult.ServiceResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotingControllerUTest {
    @Mock
    private GatheringService gatheringService;
    @Mock
    private VotingService votingService;
    @Mock
    private VoteService voteService;
    @Mock
    private ParticipationService participationService;

    @InjectMocks
    private VotingController votingController;

    @Mock
    private BindingResult bindingResult;

    @Test
    void checkWiring() {
        assertThat(votingController).isNotNull();
    }

    @Test
    void someOneTriesToVoteButParticipatesInDifferentGathering() {
        UUID proposalId = UUID.randomUUID();
        RedirectAttributes attributes = new RedirectAttributesModelMap();
        ModelMap model = new ModelMap();

        Gathering gathering = new Gathering();
        gathering.setId(UUID.randomUUID());
        VoteProposal voteProposal = VoteProposal.builder().id(proposalId).gathering(gathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.of(voteProposal));

        Gathering differentGathering = new Gathering();
        differentGathering.setId(UUID.randomUUID());
        differentGathering.setName("thisIsADifferentGatheringAsExpectedViaProposal" + voteProposal);
        Participation participation = new Participation();
        participation.setEvent(differentGathering);
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));

        final ModelAndView result = votingController.vote(proposalId, model, attributes);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/gatherings/");
        assertThat(attributes.getFlashAttributes().get(VotingController.FLASH_MESSAGE)).isEqualTo("votes.need.participation");
    }

    @Test
    void voteSuccessUseCase() {
        UUID proposalId = UUID.randomUUID();
        RedirectAttributes attributes = new RedirectAttributesModelMap();
        ModelMap model = new ModelMap();

        Gathering gathering = new Gathering();
        gathering.setId(UUID.randomUUID());
        VoteProposal voteProposal = VoteProposal.builder().id(proposalId).gathering(gathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.of(voteProposal));

        Participation participation = new Participation();
        participation.setEvent(gathering);
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));

        final ModelAndView result = votingController.vote(proposalId, model, attributes);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("votings/vote");
        assertThat(attributes.getFlashAttributes()).isEmpty();
        assertThat(model.containsAttribute("vote")).isTrue();
        assertThat(model.containsAttribute("proposal")).isTrue();
        assertThat(model.getAttribute("proposal")).isEqualTo(voteProposal);
        assertThat(model.getAttribute("vote")).isInstanceOf(VoteForm.class);
    }

    @Test
    void voteWithWrongGatheringIdFromDifferentTenant() {
        UUID proposalId = UUID.randomUUID();
        RedirectAttributes attributes = new RedirectAttributesModelMap();
        ModelMap model = new ModelMap();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.empty());

        final ModelAndView result = votingController.vote(proposalId, model, attributes);
        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo("redirect:/gatherings/");
        assertThat(attributes.getFlashAttributes()).containsKeys(VotingController.FLASH_MESSAGE);
        assertThat(attributes.getFlashAttributes().get(VotingController.FLASH_MESSAGE)).isEqualTo("error.unknownId");
    }

    @Test
    void saveVotingProposalWithWrongGatheringIdFromDifferentTenantInCaseOfErrors() {
        UUID formGatheringId = UUID.randomUUID();
        VoteProposalForm form = VoteProposalForm.builder().gatheringId(formGatheringId).build();
        when(gatheringService.getWithTenantCheck(eq(formGatheringId))).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(true);

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ExtendedModelMap();
        final ModelAndView view = votingController.saveVotingProposal(form, bindingResult, model, redirectAttributes);
        assertThat(view).isNotNull();
        assertThat(view.getViewName()).isEqualTo("redirect:/gatherings/");
        assertThat(redirectAttributes.getFlashAttributes()).containsKeys(VotingController.FLASH_MESSAGE);
        assertThat(redirectAttributes.getFlashAttributes().get(VotingController.FLASH_MESSAGE)).isEqualTo("error.unknownId");
        verifyNoInteractions(votingService);
    }

    @Test
    void deleteVotingProposalWithWrongGatheringIdFromDifferentTenant() {
        UUID proposalId = UUID.randomUUID();
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(proposalId))).thenReturn(Optional.empty());

        final RedirectView view = votingController.deleteProposal(proposalId, model, redirectAttributes);
        assertThat(view).isNotNull();
        assertThat(view.isRedirectView()).isTrue();
        assertThat(view.getUrl()).isEqualTo("/gatherings/");
        verify(voteService, times(0)).deleteVote(any());
        assertThat(redirectAttributes.getFlashAttributes()).containsKeys(VotingController.FLASH_MESSAGE);
        assertThat(redirectAttributes.getFlashAttributes().get(VotingController.FLASH_MESSAGE)).isEqualTo("error.unknownId");
    }

    @Test
    void doVotePostWithFormErrors() {
        UUID voteProposalId = UUID.randomUUID();
        VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).build();
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(bindingResult.hasErrors()).thenReturn(true);

        Option option = new Option("doVotePostWithFormErrors");
        option.setId(UUID.randomUUID());
        Set<Option> voteOptions = Set.of(option);
        VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).options(voteOptions).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        assertThat(votingController.doVote(form, bindingResult, model, redirectAttributes)).isEqualTo("votings/vote");
        assertThat(form.getAvailableOptions()).hasSize(1);
        assertThat(form.getAvailableOptions().iterator().next()).isEqualTo("doVotePostWithFormErrors");
        assertThat(form.getVoteProposalId()).isEqualTo(voteProposalId);
        assertThat(model.containsAttribute("proposal")).isTrue();
    }

    @Test
    void doVotePostWithDoubleVotePreventionTakingPlace() {
        ServiceResult doubleVotingDetected = ServiceResult.builder().status(HttpStatus.LOCKED).messageKey("doVotePostWithDoubleVotePreventionTakingPlace").build();
        UUID voteProposalId = UUID.randomUUID();
        VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).build();
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(voteService.performVote(eq(form))).thenReturn(doubleVotingDetected);
        Vote alreadyVoted = new Vote();
        when(voteService.getVoteFromUser(eq(voteProposalId))).thenReturn(Optional.of(alreadyVoted));

        Gathering gathering = new Gathering();
        VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).gathering(gathering).build();
        Participation participation = new Participation();
        participation.setEvent(gathering);
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));

        assertThat(votingController.doVote(form, bindingResult, model, redirectAttributes)).isEqualTo("votes/my-vote");
        assertThat(model.containsAttribute("vote")).isTrue();
        assertThat(model.getAttribute("vote")).isEqualTo(alreadyVoted);
    }

    @Test
    void doVotePostWithDefaultNotVotedMessageAndRedirectToProposalOverviewDueToMockingProperParticipation() {
        // explicitly do not set a messageKey to test fallback
        Gathering gathering = new Gathering();
        ServiceResult voteResult = ServiceResult.builder().status(HttpStatus.OK).build();
        UUID voteProposalId = UUID.randomUUID();
        VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).build();
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(voteService.performVote(eq(form))).thenReturn(voteResult);

        VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).gathering(gathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(voteProposalId))).thenReturn(Optional.of(voteProposal));
        Participation participation = new Participation();
        participation.setEvent(gathering);
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));

        // we cannot have a participation and return to gatherings here
        assertThat(votingController.doVote(form, bindingResult, model, redirectAttributes)).isEqualTo("redirect:/votings/proposals/null");
        assertThat(redirectAttributes.getFlashAttributes()).containsKeys(VotingController.FLASH_MESSAGE);
        assertThat(redirectAttributes.getFlashAttributes().get(VotingController.FLASH_MESSAGE)).isEqualTo("error.notVoted");
    }

    @Test
    void doVotePostSuccessWithRedirectToVotingProposalOverview() {
        // use case is URL mingling
        ServiceResult voteResult = ServiceResult.builder().status(HttpStatus.OK).messageKey("doVotePostSuccessWithRedirectToVotingProposalOverview").build();
        UUID voteProposalId = UUID.randomUUID();
        VoteForm form = VoteForm.builder().voteProposalId(voteProposalId).build();
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(voteService.performVote(eq(form))).thenReturn(voteResult);

        UUID gatheringId = UUID.randomUUID();
        Gathering validGathering = new Gathering();
        validGathering.setId(gatheringId);
        VoteProposal validVoteProposal = VoteProposal.builder().id(voteProposalId).gathering(validGathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(eq(voteProposalId))).thenReturn(Optional.of(validVoteProposal));

        Participation participation = new Participation();
        participation.setEvent(validGathering);
        when(participationService.getCurrentParticipation()).thenReturn(Optional.of(participation));

        assertThat(votingController.doVote(form, bindingResult, model, redirectAttributes)).isEqualTo("redirect:/votings/proposals/" + gatheringId);
        assertThat(redirectAttributes.getFlashAttributes()).containsKeys(VotingController.FLASH_MESSAGE);
        assertThat(redirectAttributes.getFlashAttributes().get(VotingController.FLASH_MESSAGE)).isEqualTo("doVotePostSuccessWithRedirectToVotingProposalOverview");
    }

    private UUID prepareForStartEndVotingAndReturnGatheringId(UUID voteProposalId) {
        UUID gatheringId = UUID.randomUUID();
        Gathering gathering = new Gathering();
        gathering.setId(gatheringId);
        VoteProposal voteProposal = VoteProposal.builder().id(voteProposalId).gathering(gathering).build();
        when(voteService.getVoteProposalByIdWithTenantCheck(voteProposalId)).thenReturn(Optional.of(voteProposal));
        return gatheringId;
    }

    @Test
    void startVotingWithRedirectToVotingProposalOverview() {
        UUID voteProposalId = UUID.randomUUID();
        UUID gatheringId = prepareForStartEndVotingAndReturnGatheringId(voteProposalId);

        assertThat(votingController.startVoting(voteProposalId, new RedirectAttributesModelMap()).getUrl()).isEqualTo("/votings/proposals/" + gatheringId);
    }

    @Test
    void endVotingWithRedirectToVotingProposalOverview() {
        UUID voteProposalId = UUID.randomUUID();
        UUID gatheringId = prepareForStartEndVotingAndReturnGatheringId(voteProposalId);

        assertThat(votingController.endVoting(voteProposalId, new RedirectAttributesModelMap()).getUrl()).isEqualTo("/votings/proposals/" + gatheringId);
    }
}
