package de.voteeasy.settings.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SettingKeyUsage {
    private String key;
    private Long count;
    private SettingScope scope;
    private SettingType type;
}
