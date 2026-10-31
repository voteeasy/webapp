package de.voteeasy.votes;

import de.voteeasy.user.ApplicationUserService;
import de.voteeasy.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteControllerUTest {
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VoteService voteService;
    @Mock
    private VoteAggregatorService voteAggregatorService;
    @Mock
    private ApplicationUserService applicationUserService;

    @InjectMocks
    private VoteController voteController;

    @Test
    void checkWiring() {
        assertThat(voteController).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void showAllVotes() {
        User user = new User();
        when(applicationUserService.currentlyLoggedInUser()).thenReturn(Optional.of(user));
        Vote vote = new Vote();
        vote.setId(UUID.randomUUID());
        when(voteRepository.findByVoter(eq(user))).thenReturn(Set.of(vote));

        final ModelAndView modelAndView = voteController.showAllByTenantAndUser();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getModelMap().containsAttribute("votes")).isTrue();
        assertThat(modelAndView.getModelMap().getAttribute("votes")).isInstanceOf(Set.class);
        assertThat(((Set<Vote>) modelAndView.getModelMap().getAttribute("votes"))).containsExactly(vote);
    }

}