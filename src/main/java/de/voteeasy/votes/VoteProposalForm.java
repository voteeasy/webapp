package de.voteeasy.votes;

import de.voteeasy.votes.validation.ValidVoteProposalOptions;
import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ValidVoteProposalOptions
public class VoteProposalForm {
    @Builder.Default
    @Size(min = 1, max = 10)
    @NotEmpty(message = "{NotEmpty.option.options}")
// #31: strange compile error with JDK16/17 locally, but works fine with Java11
// private List<@NotBlank(message = "{NotEmpty.option.options}") String> options = new ArrayList<>();
    private List<String> options = new ArrayList<>();

    @Size(max = 100)
    private String itemOfAgenda;

    @NotEmpty(message = "{NotEmpty.option.name}")
    @Size(min = 1, max = 100)
    private String name;

    @Builder.Default
    private boolean isAnonymized = false;

    @Builder.Default
    private boolean isAdminAllowedToVote = true;

    // convenience fields for UI
    private UUID gatheringId;
    private String gatheringName;
}
