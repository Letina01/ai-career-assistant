package com.careerassistant.controller;

import com.careerassistant.config.properties.JobApiProperties;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.job.SaveJobRequest;
import com.careerassistant.service.JobRecommendationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('APPLICANT')")
public class JobController {

    private final JobRecommendationService jobRecommendationService;
    private final JobApiProperties jobApiProperties;

    @GetMapping("/recommend")
    public List<JobRecommendationResponse> recommendJobs(
            @RequestParam @Min(1) Long resumeId,
            @RequestParam(defaultValue = "Software Engineer") @NotBlank String query,
            @RequestParam(defaultValue = "India") @NotBlank String location
    ) {
        log.info("Job search request - resumeId: {}, query: {}, location: {}", resumeId, query, location);
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

    // Debug endpoint to check API configuration
    @GetMapping("/debug/config")
    public Map<String, Object> debugConfig() {
        String apiKey = jobApiProperties.getRapidapi().getKey();
        String apiHost = jobApiProperties.getRapidapi().getHost();
        String apiUrl = jobApiProperties.getRapidapi().getUrl();

        boolean hasApiKey = StringUtils.hasText(apiKey);
        
        return Map.of(
                "apiUrl", apiUrl != null ? apiUrl : "NOT SET",
                "apiHost", apiHost != null ? apiHost : "NOT SET",
                "apiKeyConfigured", hasApiKey,
                "apiKeyFirstChars", hasApiKey ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "NOT SET",
                "status", hasApiKey ? "✅ READY" : "❌ API KEY MISSING"
        );
    }
}
