package de.voteeasy.settings.tenant.validation;

import de.voteeasy.settings.tenant.SettingPropertyForm;
import de.voteeasy.settings.tenant.SettingType;
import org.junit.jupiter.api.Test;

import static de.voteeasy.settings.tenant.validation.ValueTypeParser.*;
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

    @Test
    void isValidNumberIsTrue() {
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value("0").build())).isTrue();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Long.MIN_VALUE)).build())).isTrue();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Long.MAX_VALUE)).build())).isTrue();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Integer.MIN_VALUE)).build())).isTrue();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Integer.MAX_VALUE)).build())).isTrue();
    }

    @Test
    void isValidNumberIsFalse() {
        assertThat(isValidNumber(SettingPropertyForm.builder().build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().type(UUID).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value("NaN").build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value("").build())).isFalse();
        // special numbers do not work
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Double.MIN_VALUE)).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Double.MAX_VALUE)).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Float.MIN_VALUE)).build())).isFalse();
        assertThat(isValidNumber(SettingPropertyForm.builder().key(KEY).type(NUMERIC).value(String.valueOf(Float.MAX_VALUE)).build())).isFalse();
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