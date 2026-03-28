package com.careerassistant.repository;

import com.careerassistant.entity.SavedJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
