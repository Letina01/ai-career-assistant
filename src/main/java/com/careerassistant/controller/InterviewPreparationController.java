package com.careerassistant.controller;

import com.careerassistant.dto.interview.InterviewPreparationRequest;
import com.careerassistant.dto.interview.InterviewPreparationResponse;
import com.careerassistant.service.InterviewPreparationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class InterviewPreparationController {

    private final InterviewPreparationService interviewPreparationService;

    @PostMapping("/prepare")
    public InterviewPreparationResponse prepare(@Valid @RequestBody InterviewPreparationRequest request) {
        return interviewPreparationService.generate(request);
    }
}
