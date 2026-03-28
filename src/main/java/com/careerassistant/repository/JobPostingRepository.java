package com.careerassistant.repository;

import com.careerassistant.entity.JobPosting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);
}
