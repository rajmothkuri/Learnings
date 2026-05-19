package com.boa.paydit.dto.request;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDto(
    @NotBlank String username,
    @NotBlank String password,
    @Pattern(regexp = "USER|ADMIN", message = "Role must be USER or ADMIN")
    String role) {

    public RegisterRequestDto {
        if (role == null || role.isBlank()) {
            role = "USER";
        } else {
            role = role.toUpperCase(Locale.US);
        }
    }
}