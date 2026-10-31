package de.voteeasy.votes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VoteProposalFormValidationTest {

    private static Validator validator;
    private VoteProposalForm form;

    @BeforeAll
    static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void newForm() {
        this.form = new VoteProposalForm();
    }

    @Test
    void whenItemOfAgendaTooBig_thenValidationErrorTriggers() {
        form.setOptions(List.of("Option1"));
        form.setName("valid");

        form.setItemOfAgenda("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
        Set<ConstraintViolation<VoteProposalForm>> violations = validator.validate(form);

        assertThat(violations.size()).isEqualTo(1);
        assertThat((violations.toArray(ConstraintViolation[]::new)[0]).getMessageTemplate()).isEqualTo("{jakarta.validation.constraints.Size.message}");
    }

    @Test
    void whenOptionsIsEmpty_thenValidationErrorTriggers() {
        form.setName("valid");
        Set<ConstraintViolation<VoteProposalForm>> violations = validator.validate(form);
        assertThat(violations.size()).isEqualTo(3);

        violations.forEach(violation -> {
            switch (violation.getMessageTemplate()) {
                case "{NotEmpty.option.options}": // will occur twice
                case "{jakarta.validation.constraints.Size.message}":
                    break;
                default:
                    throw new AssertionError("Unknown message found, should not happen.");
            }
        });
    }
}