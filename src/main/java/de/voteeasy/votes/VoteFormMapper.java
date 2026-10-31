package de.voteeasy.votes;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class VoteFormMapper {

    static Set<String> extractAvailableOptions(@NotNull Set<Option> options) {
        if (options == null) {
            return Collections.emptySet();
        }

        TreeSet<String> extractedOptions = new TreeSet<>();
        options.forEach(option -> extractedOptions.add(option.getName()));
        return extractedOptions;
    }

}
