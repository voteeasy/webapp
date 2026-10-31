package de.voteeasy.votes.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidVoteProposalOptionsValidator.class)
public @interface ValidVoteProposalOptions {
    String message() default "{NotEmpty.option.options}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
