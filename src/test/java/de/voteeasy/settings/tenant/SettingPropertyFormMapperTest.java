package de.voteeasy.settings.tenant;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SettingPropertyFormMapperTest {
    final String KEY = "asdkljasdjlkasjdlaskjd.my.key.In.Tests";
    final String VALUE = UUID.randomUUID().toString();
    final String SCOPE = SettingScope.TENANT.name();
    final String TYPE = SettingType.UUID.name();

    @Test
    void toEntityWithNullKey() {
        SettingPropertyForm form = SettingPropertyForm.builder().build();
        assertThat(SettingPropertyFormMapper.toEntity(form).getKey()).isNull();
    }

    @Test
    void toEntityWithLowerCasedKey() {
        SettingPropertyForm form = SettingPropertyForm.builder().key(KEY).build();
        assertThat(SettingPropertyFormMapper.toEntity(form).getKey()).isEqualTo(KEY.toLowerCase());
    }

    @Test
    void toEntityWithWhitespacedKey() {
        SettingPropertyForm form = SettingPropertyForm.builder().key("     " + KEY + "   \t\n\r       ").build();
        assertThat(SettingPropertyFormMapper.toEntity(form).getKey()).isEqualTo(KEY.toLowerCase());
    }

    @Test
    void toEntityWithNullValue() {
        SettingPropertyForm form = SettingPropertyForm.builder().build();
        assertThat(SettingPropertyFormMapper.toEntity(form).getValue()).isNull();
    }

    @Test
    void toEntityWithLowerCasedValue() {
        SettingPropertyForm form = SettingPropertyForm.builder().value(VALUE).build();
        assertThat(SettingPropertyFormMapper.toEntity(form).getValue()).isEqualTo(VALUE.toLowerCase());
    }
}