package com.careerassistant.service.impl;

import com.careerassistant.dto.skillgap.SkillGapRequest;
import com.careerassistant.dto.skillgap.SkillGapResponse;
import com.careerassistant.entity.Resume;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.repository.ResumeRepository;
import com.careerassistant.service.AiService;
import com.careerassistant.service.SkillGapAnalyzer;
import com.careerassistant.service.SkillGapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillGapServiceImpl implements SkillGapService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AiService aiService;
    private final SkillGapAnalyzer skillGapAnalyzer;

    @Override
    public SkillGapResponse analyze(SkillGapRequest request) {
        log.info("Skill Gap Analysis requested for resumeId={}, role={}", 
                request.resumeId(), request.targetRole());

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found"));

        Resume resume = resumeRepository.findById(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        String resumeText = resume.getExtractedText();
        List<String> currentSkills = parseCurrentSkills(analysis);
        
        log.info("Current skills from resume analysis: {}", currentSkills);
        log.info("Resume text length: {}", resumeText != null ? resumeText.length() : 0);

        if (currentSkills != null && !currentSkills.isEmpty()) {
            log.info("Using skills from resume analysis");
            return skillGapAnalyzer.analyzeSkillGapWithSkills(resumeText, request.targetRole(), currentSkills);
        }

        try {
            log.info("Attempting AI-based skill gap analysis...");
            SkillGapResponse aiResponse = aiService.generateSkillGap(request.targetRole(), currentSkills);
            
            if (isValidAiResponse(aiResponse)) {
                log.info("AI analysis successful for role: {}", request.targetRole());
                return aiResponse;
            }
            
            log.warn("AI returned invalid/empty response, falling back to dynamic analysis");
        } catch (Exception aiEx) {
            log.warn("AI service failed: {} - {}", aiEx.getClass().getSimpleName(), aiEx.getMessage());
            log.info("Falling back to dynamic skill gap analysis");
        }

        log.info("Using dynamic skill gap analyzer...");
        return skillGapAnalyzer.analyzeSkillGap(resumeText, request.targetRole());
    }

    private List<String> parseCurrentSkills(ResumeAnalysis analysis) {
        if (analysis == null || analysis.getExtractedSkills() == null || 
            analysis.getExtractedSkills().isBlank()) {
            return List.of();
        }
        
        return List.of(analysis.getExtractedSkills().split(",")).stream()
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();
    }

    private boolean isValidAiResponse(SkillGapResponse response) {
        if (response == null) {
            return false;
        }
        if (response.currentSkills() == null || response.currentSkills().isEmpty()) {
            return false;
        }
        if (response.missingSkills() == null) {
            return false;
        }
        if (response.roadmap() == null || response.roadmap().isEmpty()) {
            return false;
        }
        return true;
    }
}
