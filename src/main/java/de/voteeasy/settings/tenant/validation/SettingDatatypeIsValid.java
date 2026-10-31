package de.voteeasy.settings.tenant.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SettingDatatypeIsValidValidator.class)
public @interface SettingDatatypeIsValid {
    String message() default "setting.combination.error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
