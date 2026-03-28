package com.careerassistant.service.impl;

import com.careerassistant.dto.interview.InterviewPreparationRequest;
import com.careerassistant.dto.interview.InterviewPreparationResponse;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.AiService;
import com.careerassistant.service.InterviewPreparationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InterviewPreparationServiceImpl implements InterviewPreparationService {

    private final AiService aiService;
    private final CurrentUserService currentUserService;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Override
    public InterviewPreparationResponse generate(InterviewPreparationRequest request) {
        UserAccount user = currentUserService.getCurrentUser();
        String enrichedFocusArea = buildEnrichedFocusArea(request.focusArea(), user);
        return aiService.generateInterviewPreparation(request.role(), enrichedFocusArea);
    }

    private String buildEnrichedFocusArea(String focusArea, UserAccount user) {
        List<String> contextParts = new ArrayList<>();
        contextParts.add("User focus: " + focusArea);
        if (StringUtils.hasText(user.getCurrentRole())) {
            contextParts.add("Current role: " + user.getCurrentRole());
        }
        if (user.getExperienceYears() != null) {
            contextParts.add("Experience: " + user.getExperienceYears() + " years");
        }
        if (StringUtils.hasText(user.getSkills())) {
            contextParts.add("Known skills: " + user.getSkills());
        }
        if (StringUtils.hasText(user.getPreferredRole())) {
            contextParts.add("Target preference: " + user.getPreferredRole());
        }

        resumeAnalysisRepository.findFirstByResumeOwnerIdOrderByCreatedAtDesc(user.getId())
                .ifPresent(analysis -> addResumeContext(contextParts, analysis));

        contextParts.add("Generate practical, company-style interview prep: round-wise questions, expected answer depth, and mistakes to avoid.");
        return String.join("\n", contextParts);
    }

    private void addResumeContext(List<String> contextParts, ResumeAnalysis analysis) {
        contextParts.add("Latest ATS score: " + analysis.getAtsScore());
        if (StringUtils.hasText(analysis.getExtractedSkills())) {
            contextParts.add("Resume skills: " + analysis.getExtractedSkills());
        }
        if (StringUtils.hasText(analysis.getMissingSkills())) {
            contextParts.add("Resume skill gaps: " + analysis.getMissingSkills());
        }
    }
}
