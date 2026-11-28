package de.voteeasy.settings.tenant.validation;

import de.voteeasy.settings.tenant.SettingPropertyForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class SettingDatatypeIsValidValidatorTest {
    @Mock
    private SettingDatatypeIsValid constraint;

    @Test
    void initialize() {
        assertDoesNotThrow(() -> new SettingDatatypeIsValidValidator().initialize(constraint));
    }

    @Test
    void isValidWithNullForm() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(null, null)).isFalse();
    }

    @Test
    void isValidWithFormWithoutValueAndKey() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().build(), null)).isFalse();
    }

    @Test
    void isValidWithFormWithoutKeyButValue() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().value("something").build(), null)).isFalse();
    }

    @Test
    void isValidWithFormWithoutValueButKey() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().key("someKey").build(), null)).isFalse();
    }

    @Test
    void isValidUuidForm() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().key("someKey").value(UUID.randomUUID().toString()).type("UUID").build(), null)).isTrue();
    }

    @Test
    void isValidNumberForm() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().key("someKey").value(String.valueOf(4711)).type("NUMERIC").build(), null)).isTrue();
    }

    @Test
    void isInvalidNumberForm() {
        SettingDatatypeIsValidValidator validator = new SettingDatatypeIsValidValidator();
        for (String value : new String[]{"lettersAreNotNumbers", "NaN", "1.5", "2147483648"}) {
            assertThat(validator.isValid(SettingPropertyForm.builder().key("someKey").value(value).type("NUMERIC").build(), null))
                    .as("NUMERIC value %s", value).isFalse();
        }
    }

    @Test
    void isValidAlphanumericFormWithDigits() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().key("someKey").value("42").type("ALPHANUMERIC").build(), null)).isTrue();
    }

    @Test
    void isValidAlphanumericForm() {
        assertThat(new SettingDatatypeIsValidValidator().isValid(SettingPropertyForm.builder().key("someKey").value("someValue").type("ALPHAnumeric").build(), null)).isTrue();
    }
}