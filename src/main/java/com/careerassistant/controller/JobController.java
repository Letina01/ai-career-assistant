package com.careerassistant.controller;

import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.job.SaveJobRequest;
import com.careerassistant.service.JobRecommendationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class JobController {

    private final JobRecommendationService jobRecommendationService;

    @GetMapping("/recommend")
    public List<JobRecommendationResponse> recommendJobs(
            @RequestParam Long resumeId,
            @RequestParam(defaultValue = "Software Engineer") String query,
            @RequestParam(defaultValue = "India") String location
    ) {
        return jobRecommendationService.recommendJobs(resumeId, query, location);
    }

    @PostMapping("/save")
    public JobRecommendationResponse saveJob(@Valid @RequestBody SaveJobRequest request) {
        return jobRecommendationService.saveJob(request);
    }

    @GetMapping("/saved")
    public List<JobRecommendationResponse> savedJobs() {
        return jobRecommendationService.getSavedJobs();
    }
}
