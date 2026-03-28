package com.careerassistant.repository;

import com.careerassistant.entity.ResumeAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Optional<ResumeAnalysis> findByResumeId(Long resumeId);
    Optional<ResumeAnalysis> findFirstByResumeOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
