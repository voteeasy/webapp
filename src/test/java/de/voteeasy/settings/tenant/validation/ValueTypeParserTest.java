package de.voteeasy.settings.tenant.validation;

import de.voteeasy.settings.tenant.SettingPropertyForm;
import de.voteeasy.settings.tenant.SettingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static de.voteeasy.settings.tenant.validation.ValueTypeParser.isValidAlphanumeric;
import static de.voteeasy.settings.tenant.validation.ValueTypeParser.isValidNumber;
import static de.voteeasy.settings.tenant.validation.ValueTypeParser.isValidUuid;
import static org.assertj.core.api.Assertions.assertThat;

class ValueTypeParserTest {
    private static final String KEY = "aRealKeyInTest";
    private static final String UUID = SettingType.UUID.name();
    private static final String NUMERIC = SettingType.NUMERIC.name();
    private static final String ALPHANUMERIC = SettingType.ALPHANUMERIC.name();

    @Test
    void isValidAlphanumericIsTrue() {
        assertThat(isValidAlphanumeric(SettingPropertyForm.builder().key(KEY).type(ALPHANUMERIC).value("AValueIsAValue").build())).isTrue();
        assertThat(isValidAlphanumeric(SettingPropertyForm.builder().key(KEY).type(ALPHANUMERIC).value("              AValueIsAValue      ").build())).isTrue();
        assertThat(isValidAlphanumeric(SettingPropertyForm.builder().key(KEY).type(ALPHANUMERIC).value("              A Value With Spaces Is AValue      ").build())).isTrue();
    }

    @Test
    void isValidAlphanumericIsFalse() {
        assertThat(isValidAlphanumeric(SettingPropertyForm.builder().key(KEY).type(ALPHANUMERIC).value(null).build())).isFalse();
        assertThat(isValidAlphanumeric(SettingPropertyForm.builder().key(KEY).type(ALPHANUMERIC).value("").build())).isFalse();
        assertThat(isValidAlphanumeric(SettingPropertyForm.builder().key(KEY).type(ALPHANUMERIC).value(Character.toString(' ')).build())).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "42", "-42", "+42", "-0", "+0", "0042", "-2147483648", "2147483647"})
    void isValidNumberIsTrue(String value) {
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(value).build())).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "lettersAreNotNumbers", "NaN", "Infinity", "-Infinity",
            "1.5", ".5", "1.", "1e3", "0x2A", "4.9E-324", "1.7976931348623157E308",
            "1.4E-45", "3.4028235E38", " ", " 42", "42 ", "4 2", "42\n",
            "+", "-", "--42", "+-42", "\u0664\u0662",
            "-2147483649", "2147483648", "-9223372036854775808", "9223372036854775807"
    })
    void isValidNumberRejectsInvalidValue(String value) {
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(value).build())).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ALPHANUMERIC", "UUID", "numeric", "unknown"})
    void isValidNumberRejectsNonNumericType(String type) {
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(type).value("42").build())).isFalse();
    }

    @Test
    void isValidNumberIsFalse() {
        assertThat(isValidNumber(SettingPropertyForm.builder().build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().type(UUID).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().type(NUMERIC).value("42").build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key("").type(NUMERIC).value("42").build())).isFalse();
    }

    @Test
    void isValidUuidIsFalse() {
        assertThat(isValidUuid(SettingPropertyForm.builder().build())).isFalse();
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).build())).isFalse();
        assertThat(isValidUuid(SettingPropertyForm.builder().type(UUID).build())).isFalse();
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).type(UUID).build())).isFalse();
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).type(UUID).value("noUUID").build())).isFalse();
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).type(UUID).value("").build())).isFalse();
    }

    @Test
    void isValidUuidIsTrue() {
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).type(UUID).value(java.util.UUID.randomUUID().toString()).build())).isTrue();
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).type(UUID).value("4976f8af-9c44-4e55-b335-4957ffb9edb5").build())).isTrue();
        assertThat(isValidUuid(SettingPropertyForm.builder().key(KEY).type(UUID).value("4976f8af-9c44-4e55-b335-4957ffb9edb5".toUpperCase()).build())).isTrue();
    }
}