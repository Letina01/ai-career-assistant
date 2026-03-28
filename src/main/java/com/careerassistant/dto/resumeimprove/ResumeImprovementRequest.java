package com.careerassistant.dto.resumeimprove;

import jakarta.validation.constraints.NotBlank;

public record ResumeImprovementRequest(
        Long resumeId,
        @NotBlank String sectionName,
        @NotBlank String sectionContent
) {
}
