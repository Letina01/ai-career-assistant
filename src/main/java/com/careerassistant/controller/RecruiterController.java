package com.careerassistant.controller;

import com.careerassistant.dto.recruiter.RecruiterApplicantResponse;
import com.careerassistant.dto.recruiter.RecruiterApplicationStatusRequest;
import com.careerassistant.dto.recruiter.RecruiterJobPostingRequest;
import com.careerassistant.dto.recruiter.RecruiterJobPostingResponse;
import com.careerassistant.dto.recruiter.ResumeDownloadPayload;
import com.careerassistant.service.RecruiterService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

    @PostMapping("/jobs")
    public RecruiterJobPostingResponse createJob(@Valid @RequestBody RecruiterJobPostingRequest request) {
        return recruiterService.createJob(request);
    }

    @GetMapping("/jobs")
    public List<RecruiterJobPostingResponse> getMyJobs() {
        return recruiterService.getMyJobs();
    }

    @GetMapping("/applications")
    public List<RecruiterApplicantResponse> getApplicants() {
        return recruiterService.getApplicants();
    }

    @PutMapping("/applications/{applicationId}")
    public RecruiterApplicantResponse updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody RecruiterApplicationStatusRequest request
    ) {
        return recruiterService.updateApplicationStatus(applicationId, request);
    }

    @GetMapping("/applications/{applicationId}/resume")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable Long applicationId) {
        ResumeDownloadPayload payload = recruiterService.downloadApplicantResume(applicationId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.fileName() + "\"")
                .body(new ByteArrayResource(payload.content()));
    }
}
