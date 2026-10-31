package de.voteeasy.votes;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class VoteProposalMapperTests {
    private static final String NAME = "myName";
    private static final String ITEM_OF_AGENDA = "Top 0815";
    private static final List<String> OPTIONS = List.of("e", "b", "d", "c", "a");

    @Test
    void fromDatabaseToForm() {
        VoteProposal.VoteProposalBuilder voteProposal = VoteProposal.builder().isAnonymized(TRUE).isAdminAllowedToVote(FALSE).name(NAME).itemOfAgenda(ITEM_OF_AGENDA);

        Set<Option> asOption = new TreeSet<>();
        OPTIONS.forEach(name -> asOption.add(new Option(name)));
        voteProposal.options(asOption);

        final VoteProposalForm form = VoteProposalFormMapper.toForm(voteProposal.build());
        assertThat(form).isNotNull();
        assertThat(form.getName()).isEqualTo(NAME);
        assertThat(form.getItemOfAgenda()).isEqualTo(ITEM_OF_AGENDA);
        assertThat(form.getOptions()).asInstanceOf(InstanceOfAssertFactories.LIST).hasSameElementsAs(OPTIONS);
        assertThat(form.isAnonymized()).isTrue();
        assertThat(form.isAdminAllowedToVote()).isFalse();

        assertThat(form.getGatheringId()).isNull();
        assertThat(form.getGatheringName()).isNull();
    }

    @Test
    void fromFormToDatabase() {
        VoteProposalForm voteProposalForm = VoteProposalForm.builder().gatheringId(UUID.randomUUID()).gatheringName("MyTestGathering").name(NAME).isAnonymized(false).isAdminAllowedToVote(false).itemOfAgenda(ITEM_OF_AGENDA).options(OPTIONS).build();

        final VoteProposal voteProposal = VoteProposalFormMapper.toEntity(voteProposalForm);
        assertThat(voteProposal).isNotNull();
        assertThat(voteProposal.getName()).isEqualTo(NAME);
        assertThat(voteProposal.getItemOfAgenda()).isEqualTo(ITEM_OF_AGENDA);
        assertThat(voteProposal.isAnonymized()).isFalse();
        assertThat(voteProposal.isAdminAllowedToVote()).isFalse();
    }

    @Test
    void ensureValidationWorks() {
        VoteProposalForm invalidForm = new VoteProposalForm();
        invalidForm.setName(null);

        VoteProposalFormMapper.toEntity(invalidForm);
    }
}
