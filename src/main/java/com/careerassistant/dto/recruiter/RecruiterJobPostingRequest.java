package com.careerassistant.dto.recruiter;

import jakarta.validation.constraints.NotBlank;

public record RecruiterJobPostingRequest(
        @NotBlank String title,
        @NotBlank String company,
        @NotBlank String location,
        @NotBlank String description,
        @NotBlank String requiredSkills,
        @NotBlank String applyLink
) {
}
