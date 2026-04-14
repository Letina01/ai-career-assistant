package com.careerassistant.repository;

import com.careerassistant.entity.ResumeAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.resume.id = :resumeId")
    Optional<ResumeAnalysis> findByResumeId(@Param("resumeId") Long resumeId);

    Optional<ResumeAnalysis> findFirstByResumeOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
