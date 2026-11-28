package de.voteeasy.validation;

import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidPassword {

    String message() default "{changepassword.passwordequal.error}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String field();
}