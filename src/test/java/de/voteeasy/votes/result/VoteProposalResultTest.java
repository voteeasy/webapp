package de.voteeasy.votes.result;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VoteProposalResultTest {
    private static final Long NOW = System.currentTimeMillis();

    @Test
    void verifySortingInViewWithItemOfAgenda() {
        VoteProposalResult result = VoteProposalResult.builder().stoppedAt(NOW).name("b").build();
        VoteProposalResult differentResult = VoteProposalResult.builder().stoppedAt(NOW + 1000).name("a").build();
        VoteProposalResult anotherResult = VoteProposalResult.builder().stoppedAt(NOW - 1000).name("c").build();

        final VoteResultView view = VoteResultView.builder().voteProposals(List.of(result, anotherResult, differentResult)).build();
        assertThat(view.getVoteProposals()).hasSize(3).containsExactlyInAnyOrder(anotherResult, differentResult, result);
    }

    @Test
    void verifySortingInViewWithoutItemOfAgenda() {
        VoteProposalResult result = VoteProposalResult.builder().stoppedAt(NOW).itemOfAgenda("Top 1").name("b").build();
        VoteProposalResult differentResult = VoteProposalResult.builder().stoppedAt(NOW).itemOfAgenda("Top 2").name("a").build();
        VoteProposalResult anotherResult = VoteProposalResult.builder().stoppedAt(NOW).itemOfAgenda("Top 0").name("c").build();

        final VoteResultView view = VoteResultView.builder().voteProposals(List.of(result, anotherResult, differentResult)).build();
        assertThat(view.getVoteProposals()).hasSize(3).containsExactlyInAnyOrder(anotherResult, result, differentResult);
    }

    @Test
    void verifyResultAggregationWithExampleData() {
        VoteProposalResult result = VoteProposalResult.builder().options(generateOptions()).build();
        assertThat(result.getTotalCount()).isEqualTo(3);
    }

    private Map<String, AtomicLong> generateOptions() {
        Map<String, AtomicLong> result = new HashMap<>();
        result.put("A", new AtomicLong(42));
        result.put("B", new AtomicLong(-42));
        result.put("C", new AtomicLong(-2));
        result.put("D", new AtomicLong(5));
        return result;
    }

    @Test
    void verifyResultAggregationIsNullSafeOnEmptyObjects() {
        VoteProposalResult result = VoteProposalResult.builder().build();
        assertThat(result.getTotalCount()).isEqualTo(0);

        result = VoteProposalResult.builder().options(null).build();
        assertThat(result.getTotalCount()).isEqualTo(0);
    }

    @Test
    void verifyComparison() {
        VoteProposalResult result = VoteProposalResult.builder().build();
        VoteProposalResult same = VoteProposalResult.builder().build();
        assertThat(result.equals(same)).isTrue();
    }

    @Test
    void verifyComparisonWithItemOfAgendaAgainstWithout() {
        VoteProposalResult with = VoteProposalResult.builder().stoppedAt(0L).name("same").itemOfAgenda("Top1").build();
        VoteProposalResult without = VoteProposalResult.builder().stoppedAt(0L).name("same").build();
        assertThat(Stream.of(with, without).sorted()).containsExactly(Arrays.array(with, without));
    }

    @Test
    void verifyComparisonByStoppingTime() {
        VoteProposalResult before = VoteProposalResult.builder().stoppedAt(0L).name("same").itemOfAgenda("Top1").build();
        VoteProposalResult after = VoteProposalResult.builder().stoppedAt(12L).name("same").itemOfAgenda("Top1").build();
        assertThat(Stream.of(after, before).sorted()).containsExactly(Arrays.array(before, after));
    }

}