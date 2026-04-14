package com.careerassistant.service;

import com.careerassistant.dto.resumeimprove.ResumeImprovementRequest;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.dto.resumeimprove.ResumeRewriteRequest;
import com.careerassistant.dto.resumeimprove.ResumeRewriteResponse;

public interface ResumeImprovementService {
    ResumeImprovementResponse improve(ResumeImprovementRequest request);
    
    ResumeRewriteResponse rewriteResume(ResumeRewriteRequest request);
}
