package de.voteeasy.votes;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoteProposalFormMapperTest {

    @Test
    void toEntityRemovesSpecialCharactersInVoteProposalOptions() {
        VoteProposalForm form = VoteProposalForm.builder().options(List.of("A;B", "C,D")).build();
        final VoteProposal toEntity = VoteProposalFormMapper.toEntity(form);
        assertThat(toEntity).isNotNull();
        for (Option o : toEntity.getOptions()) {
            assertThat(o.getName()).doesNotContain("C,D");
        }
    }
}