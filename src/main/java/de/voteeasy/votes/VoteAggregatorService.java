package de.voteeasy.votes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class VoteAggregatorService {
    public List<Object> getAsChart(Set<Vote> votes) {
        if (votes == null || votes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, AtomicLong> voteCardinalities = new HashMap<>();
        votes.forEach(v -> {

            // add existing keys
            v.getChosenOptions().forEach(o -> {
                        String option = o.getName();

                        if (voteCardinalities.containsKey(option)) {
                            voteCardinalities.put(option, new AtomicLong(voteCardinalities.get(option).incrementAndGet()));
                        } else {
                            voteCardinalities.put(option, new AtomicLong(1));
                        }
                    }

            );

            // if you want to add non-existing keys to be shown in the legend: we need to iterate through all options of the corresponding voteProposal
        });

        List<Object> rowElements = new ArrayList<>(votes.size());
        for (Map.Entry<String, AtomicLong> element : voteCardinalities.entrySet()) {
            rowElements.add(List.of(element.getKey(), element.getValue()));
        }

        // sort by names
        rowElements.sort(Comparator.comparing(Object::toString));

        return rowElements;

    }

}
