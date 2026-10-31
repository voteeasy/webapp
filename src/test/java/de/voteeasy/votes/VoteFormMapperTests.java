package de.voteeasy.votes;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class VoteFormMapperTests {
    private static final List<String> OPTIONS = Arrays.asList("e", "b", "c", "d", "a");

    private static Set<Option> generateOptionData() {
        Set<Option> options = new HashSet<>();
        OPTIONS.forEach(name -> options.add(new Option(name)));

        return options;
    }

    @Test
    void emptyListDuringConversion() {
        Set<String> availableOptions = VoteFormMapper.extractAvailableOptions(generateOptionData());
        assertThat(availableOptions).isNotNull().hasSize(OPTIONS.size());
        assertThat(new ArrayList<>(availableOptions)).isSorted();
    }

    @Test
    void nullValuesAreSafe() {
        VoteFormMapper.extractAvailableOptions(null);
    }

    @Test
    void classesShouldRespectBeanContract() {
        // POJO
        EqualsVerifier.forClass(VoteForm.class);
    }
}
