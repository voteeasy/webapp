package de.voteeasy.settings.tenant.validation;

import de.voteeasy.settings.tenant.SettingPropertyForm;
import de.voteeasy.settings.tenant.SettingType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;

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
    static boolean isValidAlphanumeric(SettingPropertyForm form) {
        String key = form.getKey().trim();
        String value = form.getValue() != null ? form.getValue().trim() : null;
        return SettingType.ALPHANUMERIC.equals(SettingType.parseWithDefault(form.getType())) && StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value);
    }

    static boolean isValidNumber(SettingPropertyForm form) {
        String key = form.getKey();
        String value = form.getValue();
        return SettingType.NUMERIC.equals(SettingType.parseWithDefault(form.getType())) && !StringUtils.containsWhitespace(value) && StringUtils.isNotEmpty(key) && NumberUtils.isParsable(value);
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