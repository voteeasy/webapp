package de.voteeasy.votes;

import de.voteeasy.AbstractIT;
import de.voteeasy.serviceresult.ServiceResult;
import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class VotingServiceTest extends AbstractIT {

    @Autowired
    private VotingService votingService;
    @MockitoBean
    private ApplicationUserService applicationUserService;

    @Test
    void canBeProperlyWired() {
        assertThat(votingService).isNotNull();
    }

    @Test
    void doVoteWithUnknownGatheringId() {
        ServiceResult result = votingService.addProposal(VoteProposalForm.builder().name("aValidNameIsNeeded").options(List.of("AtLeastOneOptionIsNeeded")).gatheringId(UUID.randomUUID()).build());
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getGatheringWithUnknownUUID_returnsEmptyResult() {
        assertThat(votingService.getProposalsForGathering(UUID.randomUUID())).isEmpty();
    }

    @Test
    void someoneTriesToRetrieveVoteProposalsFromADifferentTenantsGathering_returnsEmptyResult() {
        UUID gatheringIdFromDifferentTenant = UUID.randomUUID();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(new User()));
        assertThat(votingService.getProposalsForGathering(gatheringIdFromDifferentTenant)).isEmpty();
    }
}
