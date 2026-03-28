package com.careerassistant.controller;

import com.careerassistant.dto.resume.ResumeSummaryResponse;
import com.careerassistant.dto.resume.ResumeUploadResponse;
import com.careerassistant.dto.resume.ResumeAnalysisReportResponse;
import com.careerassistant.service.ResumeService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeUploadResponse uploadResume(
            @RequestParam @NotBlank String candidateName,
            @RequestParam @Email String email,
            @RequestPart MultipartFile file
    ) {
        return resumeService.uploadAndAnalyze(candidateName, email, file);
    }

    @GetMapping
    public List<ResumeSummaryResponse> getResumes() {
        return resumeService.getAllResumes();
    }

    @GetMapping("/{resumeId}/analysis")
    public ResumeAnalysisReportResponse getResumeAnalysis(@PathVariable Long resumeId) {
        return resumeService.getAnalysisReport(resumeId);
    }
}
