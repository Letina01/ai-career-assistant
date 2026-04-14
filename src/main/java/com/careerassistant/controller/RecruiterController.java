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
        
        MediaType mediaType = getMediaType(payload.fileName());
        String filename = sanitizeFilename(payload.fileName());
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(payload.content().length))
                .body(new ByteArrayResource(payload.content()));
    }

    private MediaType getMediaType(String fileName) {
        if (fileName == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        } else if (lower.endsWith(".doc")) {
            return new MediaType("application", "msword");
        } else if (lower.endsWith(".docx")) {
            return new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (lower.endsWith(".txt")) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String sanitizeFilename(String fileName) {
        if (fileName == null) {
            return "resume.pdf";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
