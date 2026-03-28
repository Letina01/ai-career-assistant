package com.careerassistant.dto.skillgap;

import jakarta.validation.constraints.NotBlank;

public record SkillGapRequest(
        Long resumeId,
        @NotBlank String targetRole
) {
}
