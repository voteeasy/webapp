package de.voteeasy.votes.result;

import lombok.Builder;
import lombok.Data;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Data
@Builder
public class VoteProposalResult implements Comparable<VoteProposalResult> {
    // VoteProposal
    private String itemOfAgenda;
    private String name;
    private Long startedAt;
    private Long stoppedAt;

    // Options
    // TODO sort by option name then by count? https://www.javatpoint.com/how-to-sort-hashmap-in-java
    @Builder.Default
    private Map<String, AtomicLong> options = new TreeMap<>();

    public Long getTotalCount() {
        LongAdder counter = new LongAdder();
        if (options != null) {
            options.entrySet().parallelStream().forEach(option -> counter.add(option.getValue().get()));
        }
        return counter.sum();
    }

    @Override
    public int compareTo(VoteProposalResult other) {
        if (itemOfAgenda == null || itemOfAgenda.isEmpty()) {
            return Comparator.comparing(VoteProposalResult::getStoppedAt).thenComparing(VoteProposalResult::getName).compare(this, other);
        }

        return Comparator.comparing(VoteProposalResult::getStoppedAt).thenComparing(VoteProposalResult::getItemOfAgenda).thenComparing(VoteProposalResult::getName).compare(this, other);
    }
}
