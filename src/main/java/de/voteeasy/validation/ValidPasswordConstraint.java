package de.voteeasy.validation;

import de.voteeasy.settings.PasswordConstraintValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Documented
public @interface ValidPasswordConstraint {
    String message() default "changepassword.passwordequal.error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
