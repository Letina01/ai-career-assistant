package com.careerassistant.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        Long sessionId,
        @NotBlank String message
) {
}
