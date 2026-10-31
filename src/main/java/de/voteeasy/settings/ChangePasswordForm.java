package de.voteeasy.settings;

import de.voteeasy.validation.DoublePasswordForm;
import de.voteeasy.validation.ValidPasswordConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@ValidPasswordConstraint
public class ChangePasswordForm implements DoublePasswordForm {
    @NotEmpty(message = "{NotEmpty.user.password}")
    @Size(min = 5, max = 20)
    @Pattern(regexp = "\\S+", message = "{changepassword.nospacesallowed}")
    private String password;

    @NotEmpty(message = "{NotEmpty.user.password}")
    @Size(min = 5, max = 20)
    @Pattern(regexp = "\\S+", message = "{changepassword.nospacesallowed}")
    private String passwordAgain;

    // convenience fields
    private UUID userId;
    private String userName;
    private String fullName;
}
