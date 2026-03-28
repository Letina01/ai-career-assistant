package com.careerassistant.service.impl;

import com.careerassistant.dto.skillgap.SkillGapRequest;
import com.careerassistant.dto.skillgap.SkillGapResponse;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.service.AiService;
import com.careerassistant.service.SkillGapService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillGapServiceImpl implements SkillGapService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AiService aiService;

    @Override
    public SkillGapResponse analyze(SkillGapRequest request) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found"));
        List<String> currentSkills = Arrays.stream(analysis.getExtractedSkills().split(","))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();
        return aiService.generateSkillGap(request.targetRole(), currentSkills);
    }
}
