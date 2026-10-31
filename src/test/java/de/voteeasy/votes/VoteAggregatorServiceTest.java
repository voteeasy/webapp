package de.voteeasy.votes;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoteAggregatorServiceTest {

    @Test
    void emptyOrNullListYieldsEmptyListAndNoException() {
        assertThat(new VoteAggregatorService().getAsChart(null)).isEmpty();
        assertThat(new VoteAggregatorService().getAsChart(Collections.emptySet())).isEmpty();
    }

    @Test
    void getChartWorksAsExpected() {
        final VoteAggregatorService service = new VoteAggregatorService();
        Set<Vote> votes = Set.of(prepareVoteOne(), prepareVoteTwo());

        final List<Object> asChart = service.getAsChart(votes);
        assertThat(asChart).isNotNull();

        assertThat(asChart).hasSize(3);
        assertThat(asChart.toString()).isEqualTo("[[A, 2], [B, 1], [C, 1]]");
    }

    private Vote prepareVoteOne() {
        Vote v = new Vote();
        v.setId(UUID.randomUUID());
        Set<Option> options = Set.of(optionA(), optionB());
        v.setChosenOptions(options);

        return v;
    }

    private Vote prepareVoteTwo() {
        Vote v = new Vote();
        v.setId(UUID.randomUUID());
        Set<Option> options = Set.of(optionA(), optionC());
        v.setChosenOptions(options);

        return v;
    }

    private Option optionA() {
        Option o = new Option();
        o.setName("A");
        o.setId(UUID.fromString("6e0ed200-8744-4d92-bdbf-8b5e7c72cdb0"));
        return o;
    }

    private Option optionB() {
        Option o = new Option();
        o.setName("B");
        o.setId(UUID.fromString("1828ac9b-1672-48fc-9577-7a3443525905"));
        return o;
    }

    private Option optionC() {
        Option o = new Option();
        o.setName("C");
        o.setId(UUID.fromString("c2e2253a-a8a0-48e5-9b89-fa6897da89bb"));
        return o;
    }
}
