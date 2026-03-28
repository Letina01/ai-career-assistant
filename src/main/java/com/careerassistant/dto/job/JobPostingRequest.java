package com.careerassistant.dto.job;

import jakarta.validation.constraints.NotBlank;

public record JobPostingRequest(
        @NotBlank String title,
        @NotBlank String company,
        @NotBlank String location,
        @NotBlank String description,
        @NotBlank String requiredSkills,
        @NotBlank String applyLink
) {
}
