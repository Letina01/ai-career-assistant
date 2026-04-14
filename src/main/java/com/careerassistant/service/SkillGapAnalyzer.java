package com.careerassistant.service;

import com.careerassistant.dto.skillgap.SkillGapResponse;
import java.util.List;
import java.util.Set;

public interface SkillGapAnalyzer {
    List<String> extractSkillsFromResume(String resumeText);
    Set<String> getRequiredSkillsForRole(String role);
    List<String> calculateMissingSkills(List<String> currentSkills, Set<String> requiredSkills);
    List<String> generateDynamicRoadmap(List<String> missingSkills, String targetRole);
    SkillGapResponse analyzeSkillGap(String resumeText, String targetRole);
    SkillGapResponse analyzeSkillGapWithSkills(String resumeText, String targetRole, List<String> providedSkills);
}
