package de.voteeasy.votes.result;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.list.TreeList;

import java.util.List;

@Data
@Builder
public class VoteResultView {
    private RelatedGathering gathering;
    @Builder.Default
    private List<VoteProposalResult> voteProposals = new TreeList<>();
    @Builder.Default
    private Long generatedAt = System.currentTimeMillis();

    boolean isEmpty() {
        return voteProposals.isEmpty();
    }
}
