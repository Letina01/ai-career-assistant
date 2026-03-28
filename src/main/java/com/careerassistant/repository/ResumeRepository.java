package com.careerassistant.repository;

import com.careerassistant.entity.Resume;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Resume> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerId(Long ownerId);
}
