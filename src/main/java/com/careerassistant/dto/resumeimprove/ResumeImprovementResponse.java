package com.careerassistant.dto.resumeimprove;

public record ResumeImprovementResponse(
        String sectionName,
        String originalContent,
        String improvedContent
) {
}
