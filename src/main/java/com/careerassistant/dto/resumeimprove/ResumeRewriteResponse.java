package com.careerassistant.dto.resumeimprove;

public record ResumeRewriteResponse(
        String improvedResume,
        String downloadUrl,
        String candidateName,
        Integer estimatedAtsImprovement
) {
}
