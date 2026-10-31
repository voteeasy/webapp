package de.voteeasy.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@Builder
public class CreateTenantForm {
    @NotEmpty(message = "{NotEmpty.tenant.name}")

    // User column has length of 100, but needs to have space for '-admin' (6)
    @Size(min = 3, max = 90)
    private String name;

    @NotEmpty(message = "{NotEmpty.tenant.adminpassword}")
    @Size(min = 5, max = 100)
    private String adminPassword;
}
