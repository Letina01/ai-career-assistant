package com.careerassistant.repository;

import com.careerassistant.entity.ApplicationStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    List<ApplicationStatusHistory> findByJobApplicationIdOrderByCreatedAtAsc(Long jobApplicationId);
}
