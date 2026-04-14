package com.careerassistant.service.impl;

import com.careerassistant.dto.ai.ResumeAnalysisResult;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.resume.ResumeAnalysisReportResponse;
import com.careerassistant.dto.resume.ResumeSummaryResponse;
import com.careerassistant.dto.resume.ResumeUploadResponse;
import com.careerassistant.entity.Resume;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.repository.ResumeRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.AiService;
import com.careerassistant.service.EmailService;
import com.careerassistant.service.JobRecommendationService;
import com.careerassistant.service.ResumeService;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AiService aiService;
    private final Tika tika;
    private final CurrentUserService currentUserService;
    private final EmailService emailService;
    private final JobRecommendationService jobRecommendationService;

    @Override
    @Transactional
    public ResumeUploadResponse uploadAndAnalyze(String candidateName, String email, MultipartFile file) {
        log.info("========== RESUME UPLOAD STARTED ==========");
        log.info("Step 1: Extracting text from file: {}", file.getOriginalFilename());
        
        // Step 1: Extract text from PDF
        String extractedText = extractText(file);
        log.info("Step 1 COMPLETE: Extracted {} characters", extractedText.length());

        // Step 2: Get current user
        log.info("Step 2: Getting current user...");
        var currentUser = currentUserService.getCurrentUser();
        log.info("Step 2 COMPLETE: User ID={}, Email={}", currentUser.getId(), currentUser.getEmail());

        // Step 3: Save resume to database
        log.info("Step 3: Saving resume to database...");
        Resume resume = new Resume();
        resume.setOwner(currentUser);
        resume.setCandidateName(candidateName);
        resume.setEmail(email);
        resume.setFileName(file.getOriginalFilename());
        resume.setExtractedText(extractedText);
        resume.setFileType(file.getContentType());
        try {
            byte[] fileBytes = file.getBytes();
            resume.setFileData(fileBytes);
            log.info("File bytes read successfully. Size: {} bytes, ContentType: {}", 
                    fileBytes.length, file.getContentType());
        } catch (IOException e) {
            log.error("Failed to read file bytes: {}", e.getMessage());
            throw new RuntimeException("Failed to process uploaded file", e);
        }
        Resume savedResume = resumeRepository.save(resume);
        log.info("Step 3 COMPLETE: Resume saved with ID={}, FileName={}, FileData length={}", 
                savedResume.getId(), savedResume.getFileName(), 
                savedResume.getFileData() != null ? savedResume.getFileData().length : "null");

        // Step 4: Analyze resume with AI
        log.info("Step 4: Analyzing resume with AI...");
        ResumeAnalysisResult result = aiService.analyzeResume(extractedText);
        log.info("Step 4 COMPLETE: ATS Score={}, Skills found={}", result.atsScore(), result.extractedSkills().size());

        // Step 5: Save analysis to database
        log.info("Step 5: Saving resume analysis...");
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResume(savedResume);
        analysis.setAtsScore(result.atsScore());
        analysis.setExtractedSkills(String.join(",", result.extractedSkills()));
        analysis.setMissingSkills(String.join(",", result.missingSkills()));
        analysis.setSuggestions(String.join("||", result.suggestions()));
        resumeAnalysisRepository.save(analysis);
        log.info("Step 5 COMPLETE: Analysis saved");

        // Step 6: Send email (make it non-blocking - wrap in try-catch)
        log.info("Step 6: Sending resume analysis email...");
        try {
            emailService.sendResumeAnalysisReport(savedResume, result);
            log.info("Step 6 COMPLETE: Email sent");
        } catch (Exception emailEx) {
            log.error("Step 6 FAILED (non-critical, continuing): {}", emailEx.getMessage());
            // Don't throw - email failure shouldn't break the whole upload
        }

        // Step 7: Get user preferences
        String preferredRole = currentUser.getPreferredRole();
        String preferredLocation = currentUser.getPreferredLocation();
        if (preferredLocation == null || preferredLocation.isBlank()) {
            preferredLocation = currentUser.getCity();
        }
        if (preferredLocation == null || preferredLocation.isBlank()) {
            preferredLocation = "Remote";
        }
        if (preferredRole == null || preferredRole.isBlank()) {
            preferredRole = "Software Engineer";
        }
        log.info("Step 7 COMPLETE: Role={}, Location={}", preferredRole, preferredLocation);

        // Step 8: Get job recommendations
        log.info("Step 8: Fetching job recommendations...");
        List<JobRecommendationResponse> recommendations = List.of();
        try {
            recommendations = jobRecommendationService.recommendJobs(
                    savedResume.getId(),
                    preferredRole,
                    preferredLocation
            );
            log.info("Step 8 COMPLETE: Found {} jobs", recommendations.size());
        } catch (Exception jobEx) {
            log.error("Step 8 FAILED (non-critical, continuing): {}", jobEx.getMessage());
            // Don't throw - job recommendation failure shouldn't break upload
        }

        // Step 9: Send job recommendations email
        log.info("Step 9: Sending job recommendations email...");
        try {
            emailService.sendJobRecommendations(savedResume, preferredRole, preferredLocation, recommendations);
            log.info("Step 9 COMPLETE: Job recommendations email sent");
        } catch (Exception emailEx) {
            log.error("Step 9 FAILED (non-critical): {}", emailEx.getMessage());
        }

        log.info("========== RESUME UPLOAD SUCCESSFUL ==========");
        
        return new ResumeUploadResponse(
                savedResume.getId(),
                savedResume.getCandidateName(),
                savedResume.getEmail(),
                savedResume.getFileName(),
                extractedText,
                result.atsScore(),
                result.extractedSkills(),
                result.missingSkills(),
                result.suggestions(),
                savedResume.getCreatedAt()
        );
    }

    @Override
    public List<ResumeSummaryResponse> getAllResumes() {
        return resumeRepository.findByOwnerIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId()).stream()
                .map(resume -> new ResumeSummaryResponse(
                        resume.getId(),
                        resume.getCandidateName(),
                        resume.getEmail(),
                        resume.getFileName(),
                        resumeAnalysisRepository.findByResumeId(resume.getId()).map(ResumeAnalysis::getAtsScore).orElse(null),
                        resume.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public ResumeAnalysisReportResponse getAnalysisReport(Long resumeId) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        Resume resume = resumeRepository.findByIdAndOwnerId(resumeId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found"));

        return new ResumeAnalysisReportResponse(
                resume.getId(),
                resume.getCandidateName(),
                resume.getFileName(),
                analysis.getAtsScore(),
                splitCsv(analysis.getExtractedSkills()),
                splitCsv(analysis.getMissingSkills()),
                splitTokenized(analysis.getSuggestions(), "\\|\\|"),
                analysis.getCreatedAt()
        );
    }

    private String extractText(MultipartFile file) {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (IOException | TikaException ex) {
            log.error("PDF EXTRACTION FAILED: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw new IllegalArgumentException("Unable to parse uploaded PDF: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("UNEXPECTED ERROR in extractText: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw new RuntimeException("Failed to extract text from PDF", ex);
        }
    }

    private List<String> splitCsv(String value) {
        return splitTokenized(value, ",");
    }

    private List<String> splitTokenized(String value, String delimiterRegex) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(delimiterRegex))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }
}
