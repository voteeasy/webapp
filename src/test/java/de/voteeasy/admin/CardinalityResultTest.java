package de.voteeasy.admin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardinalityResultTest {

    @Test
    void newResultIsEmptyAndNullSafe() {
        assertThat(new CardinalityResult().getTableCardinalities()).isEmpty();
    }

    @Test
    void newResultHasCountOfZero() {
        assertThat(new CardinalityResult().calculateAndGetTotal()).isEqualTo(0L);
    }

    @Test
    void createResultAndCalculateTotalAndVerifyChart() {
        CardinalityResult result = new CardinalityResult();
        result.put("foo", 4711L);
        result.put("bar", 815L);
        assertThat(result.getTableCardinalities()).containsEntry("foo", 4711L).containsEntry("bar", 815L);
        assertThat(result.calculateAndGetTotal()).isEqualTo(4711L + 815L);
        assertThat(result.getAsChart().toString()).isEqualTo("[[bar, 815], [foo, 4711]]");
    }
}
