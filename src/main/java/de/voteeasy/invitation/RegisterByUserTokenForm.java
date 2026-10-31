package de.voteeasy.invitation;

import de.voteeasy.validation.DoublePasswordForm;
import de.voteeasy.validation.ValidPasswordConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@Builder
@ValidPasswordConstraint
public class RegisterByUserTokenForm implements DoublePasswordForm {
    @NotEmpty(message = "{NotEmpty.register.password}")
    @Size(min = 5, max = 20)
    @Pattern(regexp = "\\S+", message = "{changepassword.nospacesallowed}")
    private String password;

    @NotEmpty(message = "{NotEmpty.register.password}")
    @Size(min = 5, max = 20)
    @Pattern(regexp = "\\S+", message = "{changepassword.nospacesallowed}")
    private String passwordAgain;

    @NotEmpty(message = "{NotEmpty.register.token}")
    private String token;
}
