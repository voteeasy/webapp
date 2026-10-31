package de.voteeasy.votes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class VoteForm {
    @Builder.Default
    private Set<String> availableOptions = new TreeSet<>();

    @Builder.Default
    @NotEmpty(message = "{NotEmpty.vote.selectedOptions}")
    private Set<String> selectedOptions = new TreeSet<>();

    // convenience fields
    private UUID voteProposalId;
}
