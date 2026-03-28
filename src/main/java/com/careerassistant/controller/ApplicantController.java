package com.careerassistant.controller;

import com.careerassistant.dto.application.JobApplicationRequest;
import com.careerassistant.dto.application.JobApplicationResponse;
import com.careerassistant.dto.application.ApplicationStatusHistoryResponse;
import com.careerassistant.dto.job.JobPostingResponse;
import com.careerassistant.service.ApplicantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applicant")
@RequiredArgsConstructor
public class ApplicantController {

    private final ApplicantService applicantService;

    @GetMapping("/jobs")
    public List<JobPostingResponse> getAvailableJobs() {
        return applicantService.getAvailableJobs();
    }

    @PostMapping("/jobs/{jobId}/apply")
    public JobApplicationResponse apply(@PathVariable Long jobId, @Valid @RequestBody JobApplicationRequest request) {
        return applicantService.apply(jobId, request);
    }

    @GetMapping("/applications")
    public List<JobApplicationResponse> getApplications() {
        return applicantService.getMyApplications();
    }

    @GetMapping("/applications/{applicationId}/history")
    public List<ApplicationStatusHistoryResponse> getApplicationHistory(@PathVariable Long applicationId) {
        return applicantService.getApplicationHistory(applicationId);
    }
}
