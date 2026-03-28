package com.careerassistant.repository;

import com.careerassistant.entity.JobApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
    List<JobApplication> findByJobPostingRecruiterIdOrderByCreatedAtDesc(Long recruiterId);
    boolean existsByJobPostingIdAndApplicantId(Long jobPostingId, Long applicantId);
}
