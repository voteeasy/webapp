package de.voteeasy.settings.tenant;

import de.voteeasy.settings.tenant.validation.SettingDatatypeIsValid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@SettingDatatypeIsValid
public class SettingPropertyForm {

    @NotEmpty(message = "{NotEmpty.setting.key}")
    @Size(min = 1, max = 99)
    private String key;

    @NotEmpty(message = "{NotEmpty.setting.value}")
    @Size(min = 1, max = 99)
    private String value;

    @NotEmpty(message = "{NotEmpty.setting.type}")
    private String type;

    @NotEmpty(message = "{NotEmpty.setting.scope}")
    private String scope;

    // convenience fields
    private UUID tenantId;
    private String tenantName;
    private UUID userId;
}
