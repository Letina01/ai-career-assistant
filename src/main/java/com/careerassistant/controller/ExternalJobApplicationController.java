package com.careerassistant.controller;

import com.careerassistant.dto.application.ApplyJobRequest;
import com.careerassistant.dto.application.ExternalJobApplicationResponse;
import com.careerassistant.service.ExternalJobApplicationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('APPLICANT')")
public class ExternalJobApplicationController {

    private final ExternalJobApplicationService applicationService;

    /**
     * Apply to an external job
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyToJob(@RequestBody ApplyJobRequest request) {
        try {
            log.info("=== JOB APPLICATION REQUEST ===");
            log.info("Job: {} | Company: {} | Location: {}", 
                    request.jobTitle(), request.company(), request.location());
            
            // Check if already applied
            if (applicationService.hasApplied(request.jobTitle(), request.company())) {
                log.warn("⚠️ User already applied to this job");
                Map<String, String> response = new HashMap<>();
                response.put("error", "You have already applied to this job");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            ExternalJobApplicationResponse response = applicationService.applyToJob(request);
            
            log.info("✅ Application created successfully. ID: {}", response.applicationId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Application submitted successfully");
            result.put("data", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (IllegalArgumentException ex) {
            log.warn("❌ Application validation error: {}", ex.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception ex) {
            log.error("❌ Failed to apply for job", ex);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to submit application");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all applications for current user
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyApplications() {
        try {
            log.info("=== FETCH MY APPLICATIONS ===");
            
            List<ExternalJobApplicationResponse> applications = 
                applicationService.getMyApplications();
            
            log.info("✅ Retrieved {} applications", applications.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", applications.size());
            response.put("data", applications);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            log.error("❌ Failed to fetch applications", ex);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to fetch applications");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get application statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getApplicationStats() {
        try {
            long totalCount = applicationService.getTotalApplicationCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalApplications", totalCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            log.error("❌ Failed to fetch application stats", ex);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to fetch statistics");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check if user has already applied to a job
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkIfApplied(@RequestBody Map<String, String> request) {
        try {
            String jobTitle = request.get("jobTitle");
            String company = request.get("company");
            
            boolean hasApplied = applicationService.hasApplied(jobTitle, company);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobTitle", jobTitle);
            response.put("company", company);
            response.put("hasApplied", hasApplied);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            log.error("❌ Failed to check application status", ex);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to check status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
