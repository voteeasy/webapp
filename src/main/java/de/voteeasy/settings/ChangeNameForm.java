package de.voteeasy.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ChangeNameForm {

    @NotEmpty(message = "{NotEmpty.user.fullName}")
    @Size(min = 1, max = 100)
    private String fullName;

    // convenience fields
    private String userName;
    private UUID userId;
}
