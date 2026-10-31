package de.voteeasy.selfservice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SelfServiceRegistrationForm {
    // User column has length of 100, but needs to have space for '-admin' (6)
    @Size(min = 3, max = 90)
    @NotEmpty(message = "{NotEmpty.selfregister.tenantname}")
    private String tenantname;

    @NotEmpty(message = "{NotEmpty.selfregister.captcha}")
    private String captcha;
}
