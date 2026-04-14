package com.careerassistant.service.impl;

import com.careerassistant.dto.resumeimprove.ResumeImprovementRequest;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.dto.resumeimprove.ResumeRewriteRequest;
import com.careerassistant.dto.resumeimprove.ResumeRewriteResponse;
import com.careerassistant.entity.Resume;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.repository.ResumeRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.AiService;
import com.careerassistant.service.ResumeDocumentService;
import com.careerassistant.service.ResumeImprovementService;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeImprovementServiceImpl implements ResumeImprovementService {

    private final AiService aiService;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ResumeDocumentService resumeDocumentService;
    private final CurrentUserService currentUserService;

    @Override
    public ResumeImprovementResponse improve(ResumeImprovementRequest request) {
        return aiService.improveResumeSection(request.sectionName(), request.sectionContent());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeRewriteResponse rewriteResume(ResumeRewriteRequest request) {
        // Fetch and validate resume
        Resume resume = resumeRepository.findById(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        
        if (!resume.getOwner().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ResourceNotFoundException("Resume is not accessible");
        }
        
        // Get resume analysis to include in context
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(request.resumeId())
                .orElse(null);
        
        // Build enriched prompt for AI
        String enrichedPrompt = buildRewritePrompt(resume, analysis, request.improvementInstructions());
        
        // Call AI to rewrite resume
        String improvedResume = aiService.rewriteResume(request.improvementInstructions(), enrichedPrompt);
        
        log.info("Resume rewritten for candidate: {}", resume.getCandidateName());
        
        // Calculate estimated ATS improvement
        Integer estimatedImprovement = calculateAtsImprovement(analysis);
        
        // Create response with data URI for display (no file save yet)
        return new ResumeRewriteResponse(
                improvedResume,
                "",
                resume.getCandidateName(),
                estimatedImprovement
        );
    }

    private String buildRewritePrompt(Resume resume, ResumeAnalysis analysis, String improvements) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("TASK: Rewrite the following resume with the specified improvements.\n\n");
        
        prompt.append("ORIGINAL RESUME:\n");
        prompt.append(resume.getExtractedText()).append("\n\n");
        
        if (analysis != null) {
            prompt.append("CURRENT RESUME ANALYSIS:\n");
            prompt.append("- ATS Score: ").append(analysis.getAtsScore()).append("/100\n");
            if (StringUtils.hasText(analysis.getExtractedSkills())) {
                prompt.append("- Current Skills: ").append(analysis.getExtractedSkills()).append("\n");
            }
            if (StringUtils.hasText(analysis.getMissingSkills())) {
                prompt.append("- Skill Gaps: ").append(analysis.getMissingSkills()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("IMPROVEMENT INSTRUCTIONS:\n");
        prompt.append(improvements).append("\n\n");
        
        prompt.append("REQUIREMENTS:\n");
        prompt.append("1. Improve action verbs and quantify achievements where possible\n");
        prompt.append("2. Add metrics and measurable results\n");
        prompt.append("3. Use strong keywords for ATS optimization\n");
        prompt.append("4. Keep the overall structure but enhance content quality\n");
        prompt.append("5. Maintain factual accuracy - don't invent false claims\n");
        prompt.append("6. Format clearly with sections for easy reading\n\n");
        
        prompt.append("Return the complete rewritten resume without any preamble or explanation.");
        
        return prompt.toString();
    }

    private Integer calculateAtsImprovement(ResumeAnalysis analysis) {
        if (analysis == null) {
            return 15; // Default improvement estimate
        }
        
        int currentScore = analysis.getAtsScore();
        if (currentScore >= 85) {
            return 5; // Limited room for improvement
        } else if (currentScore >= 70) {
            return 10;
        } else if (currentScore >= 50) {
            return 15;
        } else {
            return 20; // Significant potential improvement
        }
    }
}
