package com.careerassistant.dto.skillgap;

import java.util.List;

public record SkillGapResponse(
        String targetRole,
        List<String> currentSkills,
        List<String> missingSkills,
        List<String> roadmap
) {
}
