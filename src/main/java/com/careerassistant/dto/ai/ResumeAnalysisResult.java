package com.careerassistant.dto.ai;

import java.util.List;

public record ResumeAnalysisResult(
        List<String> extractedSkills,
        Integer atsScore,
        List<String> missingSkills,
        List<String> suggestions
) {
}
