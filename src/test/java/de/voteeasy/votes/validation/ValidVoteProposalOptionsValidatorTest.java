package de.voteeasy.votes.validation;

import de.voteeasy.votes.VoteProposalForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ValidVoteProposalOptionsValidatorTest {
    @Mock
    private ValidVoteProposalOptions constraint;

    @Test
    void initialize() {
        assertDoesNotThrow(() -> new ValidVoteProposalOptionsValidator().initialize(constraint));
    }

    @Test
    void isValidWithNullForm() {
        assertThat(new ValidVoteProposalOptionsValidator().isValid(null, null)).isFalse();
    }

    @Test
    void isValidWithOptionsSetToNull() {
        assertThat(new ValidVoteProposalOptionsValidator().isValid(VoteProposalForm.builder().options(null).build(), null)).isFalse();
    }

    @Test
    void isValidWithEmptyOptions() {
        assertThat(new ValidVoteProposalOptionsValidator().isValid(VoteProposalForm.builder().build(), null)).isFalse();
    }

    @Test
    void isValidWithWhitespaceElement() {
        assertThat(new ValidVoteProposalOptionsValidator().isValid(VoteProposalForm.builder().options(List.of(" ")).build(), null)).isFalse();
    }

    @Test
    void isValidWithNullElement() {
        assertThat(new ValidVoteProposalOptionsValidator().isValid(VoteProposalForm.builder().options(List.of(" ", "   ")).build(), null)).isFalse();
    }

    @Test
    void isValidWithElementWithWhitespacesNullElement() {
        assertThat(new ValidVoteProposalOptionsValidator().isValid(VoteProposalForm.builder().options(List.of(" valid  ")).build(), null)).isTrue();
    }
}