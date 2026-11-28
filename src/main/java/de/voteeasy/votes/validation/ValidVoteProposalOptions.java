package de.voteeasy.votes.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidVoteProposalOptionsValidator.class)
public @interface ValidVoteProposalOptions {
    String message() default "{NotEmpty.option.options}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
