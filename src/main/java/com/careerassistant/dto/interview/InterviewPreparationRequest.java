package com.careerassistant.dto.interview;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InterviewPreparationRequest(
        @Min(1) Long resumeId,
        @NotBlank String targetRole,
        @NotBlank String focusArea
) {
}
