package com.careerassistant.service.impl;

import com.careerassistant.dto.interview.InterviewPreparationRequest;
import com.careerassistant.dto.interview.InterviewPreparationResponse;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.AiService;
import com.careerassistant.service.InterviewPreparationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InterviewPreparationServiceImpl implements InterviewPreparationService {

    private final AiService aiService;
    private final CurrentUserService currentUserService;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Override
    @Transactional(readOnly = true)
    public InterviewPreparationResponse generate(InterviewPreparationRequest request) {
        UserAccount user = currentUserService.getCurrentUser();
        
        // Fetch resume analysis for the specified resumeId
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found for resumeId=" + request.resumeId()));
        
        // Verify resume ownership
        if (!analysis.getResume().getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Resume is not accessible");
        }
        
        String enrichedContext = buildEnrichedContext(request, user, analysis);
        return aiService.generateInterviewPreparation(request.targetRole(), enrichedContext);
    }

    private String buildEnrichedContext(InterviewPreparationRequest request, UserAccount user, ResumeAnalysis analysis) {
        List<String> contextParts = new ArrayList<>();
        
        contextParts.add("=== CANDIDATE BACKGROUND ===");
        if (StringUtils.hasText(user.getCurrentRole())) {
            contextParts.add("Current role: " + user.getCurrentRole());
        }
        if (user.getExperienceYears() != null) {
            contextParts.add("Years of experience: " + user.getExperienceYears());
        }
        
        contextParts.add("\n=== RESUME ANALYSIS ===");
        contextParts.add("Resume ATS score: " + analysis.getAtsScore() + "/100");
        if (StringUtils.hasText(analysis.getExtractedSkills())) {
            contextParts.add("Current skills: " + analysis.getExtractedSkills());
        }
        if (StringUtils.hasText(analysis.getMissingSkills())) {
            contextParts.add("Skill gaps: " + analysis.getMissingSkills());
        }
        
        contextParts.add("\n=== TARGET ROLE ===");
        contextParts.add("Target position: " + request.targetRole());
        contextParts.add("Interview focus: " + request.focusArea());
        
        contextParts.add("\n=== INSTRUCTIONS ===");
        contextParts.add("Generate a comprehensive interview preparation guide:");
        contextParts.add("1. Create 5-7 practical interview questions specific to " + request.targetRole());
        contextParts.add("2. For each question, provide a strong answer using candidate's existing skills from: " + analysis.getExtractedSkills());
        contextParts.add("3. Address skill gaps where applicable: " + analysis.getMissingSkills());
        contextParts.add("4. Include common mistakes to avoid for this role");
        contextParts.add("5. Create a 6-week learning roadmap to address gaps: " + analysis.getMissingSkills());
        contextParts.add("6. Format as strict JSON with keys: role, questionAnswers (array of {question, answer}), roadmap (array of weekly steps)");
        
        return String.join("\n", contextParts);
    }
}
