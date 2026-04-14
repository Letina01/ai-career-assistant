package com.careerassistant.service;

import com.careerassistant.dto.ai.ResumeAnalysisResult;
import com.careerassistant.dto.interview.InterviewPreparationResponse;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.dto.skillgap.SkillGapResponse;
import java.util.List;

public interface AiService {
    ResumeAnalysisResult analyzeResume(String resumeText);
    String chat(String prompt, List<String> history);
    InterviewPreparationResponse generateInterviewPreparation(String role, String focusArea);
    SkillGapResponse generateSkillGap(String targetRole, List<String> currentSkills);
    ResumeImprovementResponse improveResumeSection(String sectionName, String originalContent);
    String rewriteResume(String improvementInstructions, String resumeContext);
}
