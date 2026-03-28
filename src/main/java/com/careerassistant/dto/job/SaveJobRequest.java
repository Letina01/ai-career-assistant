package com.careerassistant.dto.job;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SaveJobRequest(
        @NotBlank String title,
        @NotBlank String company,
        @NotBlank String applyLink,
        @Min(0) @Max(100) Integer matchScore
) {
}
