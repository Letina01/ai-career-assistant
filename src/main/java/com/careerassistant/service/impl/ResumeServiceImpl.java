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
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
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
        String extractedText = extractText(file);

        Resume resume = new Resume();
        resume.setOwner(currentUserService.getCurrentUser());
        resume.setCandidateName(candidateName);
        resume.setEmail(email);
        resume.setFileName(file.getOriginalFilename());
        resume.setExtractedText(extractedText);
        Resume savedResume = resumeRepository.save(resume);

        ResumeAnalysisResult result = aiService.analyzeResume(extractedText);
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResume(savedResume);
        analysis.setAtsScore(result.atsScore());
        analysis.setExtractedSkills(String.join(",", result.extractedSkills()));
        analysis.setMissingSkills(String.join(",", result.missingSkills()));
        analysis.setSuggestions(String.join("||", result.suggestions()));
        resumeAnalysisRepository.save(analysis);
        emailService.sendResumeAnalysisReport(savedResume, result);

        String preferredRole = resume.getOwner().getPreferredRole();
        String preferredLocation = resume.getOwner().getPreferredLocation();
        if (preferredLocation == null || preferredLocation.isBlank()) {
            preferredLocation = resume.getOwner().getCity();
        }
        if (preferredLocation == null || preferredLocation.isBlank()) {
            preferredLocation = "Remote";
        }
        List<JobRecommendationResponse> recommendations =
                jobRecommendationService.recommendJobs(
                        savedResume.getId(),
                        preferredRole == null || preferredRole.isBlank() ? "Software Engineer" : preferredRole,
                        preferredLocation
                );
        emailService.sendJobRecommendations(savedResume, preferredRole, preferredLocation, recommendations);

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
            throw new IllegalArgumentException("Unable to parse uploaded PDF");
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
