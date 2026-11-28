package de.voteeasy.settings.tenant.validation;

import de.voteeasy.settings.tenant.SettingPropertyForm;
import de.voteeasy.settings.tenant.SettingType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import java.util.regex.Pattern;

import static de.voteeasy.settings.tenant.validation.ValueTypeParser.*;

@Slf4j
public class SettingDatatypeIsValidValidator implements ConstraintValidator<SettingDatatypeIsValid, SettingPropertyForm> {

    public void initialize(SettingDatatypeIsValid constraint) {
        log.debug("Settings validator started.");
    }

    @Override
    public boolean isValid(SettingPropertyForm value, ConstraintValidatorContext context) {
        if (value == null || value.getKey() == null || value.getValue() == null) {
            return false;
        }

        return isValidUuid(value) || isValidNumber(value) || isValidAlphanumeric(value);
    }

}

class ValueTypeParser {
    private static final Pattern INTEGER_VALUE = Pattern.compile("[+-]?[0-9]+");

    static boolean isValidAlphanumeric(SettingPropertyForm form) {
        String key = form.getKey().trim();
        String value = form.getValue() != null ? form.getValue().trim() : null;
        return SettingType.ALPHANUMERIC.equals(SettingType.parseWithDefault(form.getType())) && StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value);
    }

    static boolean isValidNumber(SettingPropertyForm form) {
        String key = form.getKey();
        String value = form.getValue();
        if (!SettingType.NUMERIC.equals(SettingType.parseWithDefault(form.getType()))
                || StringUtils.isEmpty(key) || value == null || !INTEGER_VALUE.matcher(value).matches()) {
            return false;
        }

        try {
            // The regex checks the format; parsing checks the int range used by ReadSettingService.
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static boolean isValidUuid(SettingPropertyForm form) {
        String key = form.getKey();
        UUID value = null;
        try {
            value = UUID.fromString(form.getValue());
        } catch (IllegalArgumentException | NullPointerException e) {
            // value remains null
        }
        return SettingType.UUID.equals(SettingType.parseWithDefault(form.getType())) && !StringUtils.containsWhitespace(key) && value != null && StringUtils.isNotEmpty(key);
    }
}