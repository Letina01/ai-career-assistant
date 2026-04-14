package com.careerassistant.service.impl;

import com.careerassistant.dto.recruiter.RecruiterApplicantResponse;
import com.careerassistant.dto.recruiter.RecruiterApplicationStatusRequest;
import com.careerassistant.dto.recruiter.RecruiterJobPostingRequest;
import com.careerassistant.dto.recruiter.RecruiterJobPostingResponse;
import com.careerassistant.dto.recruiter.ResumeDownloadPayload;
import com.careerassistant.entity.ApplicationStatus;
import com.careerassistant.entity.ApplicationStatusHistory;
import com.careerassistant.entity.JobApplication;
import com.careerassistant.entity.JobPosting;
import com.careerassistant.entity.Resume;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ApplicationStatusHistoryRepository;
import com.careerassistant.repository.JobApplicationRepository;
import com.careerassistant.repository.JobPostingRepository;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.EmailService;
import com.careerassistant.service.RecruiterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
@Slf4j
public class RecruiterServiceImpl implements RecruiterService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final CurrentUserService currentUserService;
    private final EmailService emailService;
    private final PdfGeneratorService pdfGeneratorService;

    @Override
    public RecruiterJobPostingResponse createJob(RecruiterJobPostingRequest request) {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setRecruiter(currentUserService.getCurrentUser());
        jobPosting.setTitle(request.title());
        jobPosting.setCompany(request.company());
        jobPosting.setLocation(request.location());
        jobPosting.setDescription(request.description());
        jobPosting.setRequiredSkills(request.requiredSkills());
        jobPosting.setApplyLink(request.applyLink());
        return toResponse(jobPostingRepository.save(jobPosting));
    }

    @Override
    public List<RecruiterJobPostingResponse> getMyJobs() {
        return jobPostingRepository.findByRecruiterIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RecruiterApplicantResponse> getApplicants() {
        return jobApplicationRepository.findByJobPostingRecruiterIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId())
                .stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Override
    public RecruiterApplicantResponse updateApplicationStatus(Long applicationId, RecruiterApplicationStatusRequest request) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getJobPosting().getRecruiter().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ResourceNotFoundException("Application not accessible");
        }
        ApplicationStatus previousStatus = application.getStatus();
        if (request.status() == null) {
            throw new IllegalArgumentException("Application status is required");
        }
        if (previousStatus == request.status()) {
            return toApplicationResponse(application);
        }
        application.setStatus(request.status());
        JobApplication updated = jobApplicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setJobApplication(updated);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(updated.getStatus());
        history.setChangedBy(currentUserService.getCurrentUser());
        history.setNote("Updated by recruiter");
        applicationStatusHistoryRepository.save(history);

        emailService.sendApplicationStatusUpdate(updated, previousStatus, updated.getStatus());
        return toApplicationResponse(updated);
    }

    @Override
    public ResumeDownloadPayload downloadApplicantResume(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getJobPosting().getRecruiter().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ResourceNotFoundException("Application not accessible");
        }

        Resume resume = application.getResume();
        String candidateName = application.getApplicant().getFullName();
        
        log.info("Resume download - Candidate: {}, Resume ID: {}, FileName: {}, FileData length: {}", 
                candidateName, resume.getId(), resume.getFileName(), 
                resume.getFileData() != null ? resume.getFileData().length : "null");
        
        if (resume.getFileData() != null && resume.getFileData().length > 0) {
            log.info("Downloading ORIGINAL resume file for candidate: {}", candidateName);
            String originalFileName = resume.getFileName();
            return new ResumeDownloadPayload(originalFileName, resume.getFileData());
        }
        
        log.info("Original file not found in DB, generating PDF from extracted text for candidate: {}", candidateName);
        String content = resume.getExtractedText();
        if (content == null || content.isBlank()) {
            content = "No extracted resume text available.";
        }
        byte[] pdfBytes = pdfGeneratorService.generateResumePdf(candidateName, content);
        String fileName = candidateName.replace(" ", "_") + "_resume.pdf";
        return new ResumeDownloadPayload(fileName, pdfBytes);
    }

    private RecruiterJobPostingResponse toResponse(JobPosting jobPosting) {
        return new RecruiterJobPostingResponse(
                jobPosting.getId(),
                jobPosting.getTitle(),
                jobPosting.getCompany(),
                jobPosting.getLocation(),
                jobPosting.getDescription(),
                jobPosting.getRequiredSkills(),
                jobPosting.getApplyLink(),
                jobPosting.getRecruiter().getFullName(),
                jobPosting.getCreatedAt()
        );
    }

    private RecruiterApplicantResponse toApplicationResponse(JobApplication application) {
        Integer atsScore = resumeAnalysisRepository.findByResumeId(application.getResume().getId())
                .map(analysis -> analysis.getAtsScore())
                .orElse(null);
        return new RecruiterApplicantResponse(
                application.getId(),
                application.getJobPosting().getId(),
                application.getJobPosting().getTitle(),
                application.getJobPosting().getCompany(),
                application.getApplicant().getFullName(),
                application.getApplicant().getEmail(),
                application.getApplicant().getPhone(),
                application.getApplicant().getCity(),
                application.getApplicant().getCurrentRole(),
                application.getApplicant().getCurrentCompany(),
                application.getApplicant().getExperienceYears(),
                application.getApplicant().getSkills(),
                application.getApplicant().getEducation(),
                application.getApplicant().getLinkedinUrl(),
                application.getApplicant().getGithubUrl(),
                application.getApplicant().getPortfolioUrl(),
                application.getApplicant().getBio(),
                application.getResume().getId(),
                atsScore,
                application.getStatus().name(),
                application.getCreatedAt()
        );
    }
}
