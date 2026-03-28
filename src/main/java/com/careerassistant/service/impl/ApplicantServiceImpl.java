package com.careerassistant.service.impl;

import com.careerassistant.dto.application.JobApplicationRequest;
import com.careerassistant.dto.application.JobApplicationResponse;
import com.careerassistant.dto.application.ApplicationStatusHistoryResponse;
import com.careerassistant.entity.ApplicationStatusHistory;
import com.careerassistant.dto.job.JobPostingResponse;
import com.careerassistant.entity.ApplicationStatus;
import com.careerassistant.entity.JobApplication;
import com.careerassistant.entity.JobPosting;
import com.careerassistant.entity.Resume;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.JobApplicationRepository;
import com.careerassistant.repository.JobPostingRepository;
import com.careerassistant.repository.ApplicationStatusHistoryRepository;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.repository.ResumeRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.ApplicantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class ApplicantServiceImpl implements ApplicantService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<JobPostingResponse> getAvailableJobs() {
        return jobPostingRepository.findAll().stream()
                .map(job -> new JobPostingResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getDescription(),
                        job.getRequiredSkills(),
                        job.getApplyLink(),
                        job.getRecruiter().getFullName(),
                        job.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public JobApplicationResponse apply(Long jobId, JobApplicationRequest request) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (jobApplicationRepository.existsByJobPostingIdAndApplicantId(jobId, currentUserId)) {
            throw new IllegalArgumentException("You have already applied for this job");
        }
        Resume resume = resumeRepository.findById(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        if (!resume.getOwner().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Resume not accessible");
        }
        JobApplication application = new JobApplication();
        application.setJobPosting(job);
        application.setApplicant(currentUserService.getCurrentUser());
        application.setResume(resume);
        application.setStatus(ApplicationStatus.APPLIED);
        JobApplication savedApplication = jobApplicationRepository.save(application);
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setJobApplication(savedApplication);
        history.setPreviousStatus(null);
        history.setNewStatus(ApplicationStatus.APPLIED);
        history.setChangedBy(savedApplication.getApplicant());
        history.setNote("Application submitted");
        applicationStatusHistoryRepository.save(history);
        return toResponse(savedApplication);
    }

    @Override
    public List<JobApplicationResponse> getMyApplications() {
        return jobApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ApplicationStatusHistoryResponse> getApplicationHistory(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getApplicant().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ResourceNotFoundException("Application not accessible");
        }
        return applicationStatusHistoryRepository.findByJobApplicationIdOrderByCreatedAtAsc(applicationId)
                .stream()
                .map(history -> new ApplicationStatusHistoryResponse(
                        history.getPreviousStatus() == null ? null : history.getPreviousStatus().name(),
                        history.getNewStatus().name(),
                        history.getChangedBy().getFullName(),
                        history.getNote(),
                        history.getCreatedAt()
                ))
                .toList();
    }

    private JobApplicationResponse toResponse(JobApplication application) {
        Integer atsScore = resumeAnalysisRepository.findByResumeId(application.getResume().getId())
                .map(analysis -> analysis.getAtsScore())
                .orElse(null);
        return new JobApplicationResponse(
                application.getId(),
                application.getJobPosting().getId(),
                application.getJobPosting().getTitle(),
                application.getJobPosting().getCompany(),
                application.getApplicant().getFullName(),
                application.getApplicant().getEmail(),
                application.getResume().getId(),
                atsScore,
                application.getStatus().name(),
                application.getCreatedAt()
        );
    }
}
