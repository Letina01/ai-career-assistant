package com.careerassistant.dto.auth;

public record RequestPasswordResetResponse(
        String message,
        String resetToken
) {
}
