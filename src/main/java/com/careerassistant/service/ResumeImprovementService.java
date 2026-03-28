package com.careerassistant.service;

import com.careerassistant.dto.resumeimprove.ResumeImprovementRequest;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;

public interface ResumeImprovementService {
    ResumeImprovementResponse improve(ResumeImprovementRequest request);
}
