package de.voteeasy.settings.tenant;

public enum SettingType {
    NUMERIC,
    ALPHANUMERIC,
    UUID;

    public static SettingType parseWithDefault(String type) {
        try {
            return SettingType.valueOf(type);
        } catch (NullPointerException | IllegalArgumentException e) {
            return ALPHANUMERIC;
        }
    }

}
