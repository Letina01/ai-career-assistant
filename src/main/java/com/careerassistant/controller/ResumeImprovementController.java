package com.careerassistant.controller;

import com.careerassistant.dto.resumeimprove.ResumeImprovementRequest;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.dto.resumeimprove.ResumeRewriteRequest;
import com.careerassistant.dto.resumeimprove.ResumeRewriteResponse;
import com.careerassistant.service.ResumeDocumentService;
import com.careerassistant.service.ResumeImprovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume-improvements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class ResumeImprovementController {

    private final ResumeImprovementService resumeImprovementService;
    private final ResumeDocumentService resumeDocumentService;

    @PostMapping
    public ResumeImprovementResponse improve(@Valid @RequestBody ResumeImprovementRequest request) {
        return resumeImprovementService.improve(request);
    }

    @PostMapping("/rewrite")
    public ResumeRewriteResponse rewrite(@Valid @RequestBody ResumeRewriteRequest request) {
        return resumeImprovementService.rewriteResume(request);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadResume(
            @RequestParam String resumeText,
            @RequestParam String candidateName,
            @RequestParam(defaultValue = "pdf") String format
    ) {
        byte[] fileContent;
        String mimeType;
        String fileExtension;

        if ("docx".equalsIgnoreCase(format)) {
            fileContent = resumeDocumentService.generateDocx(resumeText, candidateName);
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            fileExtension = ".docx";
        } else {
            fileContent = resumeDocumentService.generatePdf(resumeText, candidateName);
            mimeType = "application/pdf";
            fileExtension = ".pdf";
        }

        String filename = candidateName.replaceAll("\\s+", "_") + "_resume" + fileExtension;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileContent);
    }
}
