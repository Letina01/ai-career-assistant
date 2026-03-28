package com.careerassistant.repository;

import com.careerassistant.entity.ChatSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
}
