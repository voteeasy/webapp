package de.voteeasy.settings.tenant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@Validated
class SettingPropertyFormMapper {

    private static String trimToLowerCaseOrNullIfEmpty(String input) {
        if (input != null) {
            return input.trim().toLowerCase();
        }
        return null;
    }

    static Setting toEntity(@NonNull @Valid SettingPropertyForm form) {
        final Setting.SettingBuilder settingBuilder = Setting.builder();

        settingBuilder.key(StringUtils.deleteWhitespace(trimToLowerCaseOrNullIfEmpty(form.getKey())));
        settingBuilder.value(trimToLowerCaseOrNullIfEmpty(form.getValue()));
        settingBuilder.scope(SettingScope.TENANT);
        settingBuilder.type(SettingType.parseWithDefault(form.getType()));

        return settingBuilder.build();
    }
}
