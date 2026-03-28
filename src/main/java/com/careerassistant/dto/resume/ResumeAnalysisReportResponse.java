package com.careerassistant.dto.resume;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeAnalysisReportResponse(
        Long resumeId,
        String candidateName,
        String fileName,
        Integer atsScore,
        List<String> extractedSkills,
        List<String> missingSkills,
        List<String> suggestions,
        LocalDateTime analyzedAt
) {
}
