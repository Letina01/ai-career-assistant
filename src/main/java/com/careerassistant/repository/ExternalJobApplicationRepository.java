package com.careerassistant.repository;

import com.careerassistant.entity.ExternalJobApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalJobApplicationRepository extends JpaRepository<ExternalJobApplication, Long> {

    /**
     * Find all applications for a specific user, ordered by creation date (newest first)
     */
    List<ExternalJobApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    /**
     * Check if user already applied to this job
     */
    boolean existsByApplicantIdAndJobTitleAndCompany(Long applicantId, String jobTitle, String company);

    /**
     * Find a specific application by user, job title and company
     */
    Optional<ExternalJobApplication> findByApplicantIdAndJobTitleAndCompany(Long applicantId, String jobTitle, String company);

    /**
     * Find applications with specific status for a user
     */
    @Query("SELECT a FROM ExternalJobApplication a WHERE a.applicant.id = ?1 AND a.applicationStatus = ?2 ORDER BY a.createdAt DESC")
    List<ExternalJobApplication> findByApplicantIdAndStatus(Long applicantId, String status);

    /**
     * Count total applications for a user
     */
    long countByApplicantId(Long applicantId);
}
