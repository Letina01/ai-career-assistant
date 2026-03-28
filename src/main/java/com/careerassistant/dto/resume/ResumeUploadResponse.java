package com.careerassistant.dto.resume;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeUploadResponse(
        Long resumeId,
        String candidateName,
        String email,
        String fileName,
        String extractedText,
        Integer atsScore,
        List<String> extractedSkills,
        List<String> missingSkills,
        List<String> suggestions,
        LocalDateTime createdAt
) {
}
