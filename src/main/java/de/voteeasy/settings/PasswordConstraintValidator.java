package de.voteeasy.settings;

import de.voteeasy.validation.DoublePasswordForm;
import de.voteeasy.validation.ValidPasswordConstraint;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
public class PasswordConstraintValidator implements ConstraintValidator<ValidPasswordConstraint, DoublePasswordForm> {

    @Override
    public void initialize(final ValidPasswordConstraint constraint) {
        log.info("PasswordConstraintValidator is initialized.");
    }

    @Override
    public boolean isValid(final DoublePasswordForm changePasswordForm, final ConstraintValidatorContext context) {
        return Objects.equals(changePasswordForm.getPassword(), changePasswordForm.getPasswordAgain());
    }
}