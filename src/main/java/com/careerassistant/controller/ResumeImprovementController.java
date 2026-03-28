package com.careerassistant.controller;

import com.careerassistant.dto.resumeimprove.ResumeImprovementRequest;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.service.ResumeImprovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume-improvements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class ResumeImprovementController {

    private final ResumeImprovementService resumeImprovementService;

    @PostMapping
    public ResumeImprovementResponse improve(@Valid @RequestBody ResumeImprovementRequest request) {
        return resumeImprovementService.improve(request);
    }
}
