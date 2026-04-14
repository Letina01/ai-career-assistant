package com.careerassistant.service.impl;

import com.careerassistant.dto.application.ApplyJobRequest;
import com.careerassistant.dto.application.ExternalJobApplicationResponse;
import com.careerassistant.entity.ExternalJobApplication;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ExternalJobApplicationRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.ExternalJobApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalJobApplicationServiceImpl implements ExternalJobApplicationService {

    private final ExternalJobApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public ExternalJobApplicationResponse applyToJob(ApplyJobRequest request) {
        UserAccount currentUser = currentUserService.getCurrentUser();
        
        log.info("User {} attempting to apply for job: {} at {}", 
                currentUser.getEmail(), request.jobTitle(), request.company());

        // Check if already applied
        if (applicationRepository.existsByApplicantIdAndJobTitleAndCompany(
                currentUser.getId(), request.jobTitle(), request.company())) {
            log.warn("User {} already applied for job: {} at {}", 
                    currentUser.getEmail(), request.jobTitle(), request.company());
            throw new IllegalArgumentException("You have already applied to this job");
        }

        // Create application
        ExternalJobApplication application = new ExternalJobApplication(
            currentUser,
            request.jobTitle(),
            request.company(),
            request.location(),
            request.applyLink(),
            request.matchScore()
        );

        ExternalJobApplication saved = applicationRepository.save(application);
        
        log.info("✅ Job application created successfully. Application ID: {}, Job: {} at {}", 
                saved.getId(), saved.getJobTitle(), saved.getCompany());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalJobApplicationResponse> getMyApplications() {
        UserAccount currentUser = currentUserService.getCurrentUser();
        
        log.info("Fetching applications for user: {}", currentUser.getEmail());
        
        List<ExternalJobApplication> applications = 
            applicationRepository.findByApplicantIdOrderByCreatedAtDesc(currentUser.getId());
        
        log.info("Retrieved {} applications for user: {}", 
                applications.size(), currentUser.getEmail());
        
        return applications.stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalApplicationCount() {
        UserAccount currentUser = currentUserService.getCurrentUser();
        return applicationRepository.countByApplicantId(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApplied(String jobTitle, String company) {
        UserAccount currentUser = currentUserService.getCurrentUser();
        return applicationRepository.existsByApplicantIdAndJobTitleAndCompany(
            currentUser.getId(), jobTitle, company);
    }

    private ExternalJobApplicationResponse mapToResponse(ExternalJobApplication application) {
        return new ExternalJobApplicationResponse(
            application.getId(),
            application.getJobTitle(),
            application.getCompany(),
            application.getLocation(),
            application.getApplyLink(),
            application.getMatchScore(),
            application.getApplicationStatus(),
            application.getCreatedAt()
        );
    }
}
