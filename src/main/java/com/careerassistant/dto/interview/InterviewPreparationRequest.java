package com.careerassistant.dto.interview;

import jakarta.validation.constraints.NotBlank;

public record InterviewPreparationRequest(
        @NotBlank String role,
        @NotBlank String focusArea
) {
}
