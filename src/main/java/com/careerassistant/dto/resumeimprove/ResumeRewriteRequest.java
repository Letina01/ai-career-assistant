package com.careerassistant.dto.resumeimprove;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ResumeRewriteRequest(
        @Min(1) Long resumeId,
        @NotBlank String improvementInstructions
) {
}
