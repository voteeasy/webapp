package de.voteeasy.validation;

/**
 * Interface to define forms that have a double input of passwords in order to validate new passwords for equality.
 */
public interface DoublePasswordForm {
    /**
     * @return value of the master password field.
     */
    String getPassword();

    /**
     * @return value of the password-validation/reenter password field.
     */
    String getPasswordAgain();
}
