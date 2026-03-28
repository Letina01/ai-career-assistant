package com.careerassistant.service.impl;

import com.careerassistant.dto.resumeimprove.ResumeImprovementRequest;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.service.AiService;
import com.careerassistant.service.ResumeImprovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeImprovementServiceImpl implements ResumeImprovementService {

    private final AiService aiService;

    @Override
    public ResumeImprovementResponse improve(ResumeImprovementRequest request) {
        return aiService.improveResumeSection(request.sectionName(), request.sectionContent());
    }
}
