package com.careerassistant.dto.auth;

import com.careerassistant.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email String email,
        @NotBlank String password,
        Role role
) {
}
