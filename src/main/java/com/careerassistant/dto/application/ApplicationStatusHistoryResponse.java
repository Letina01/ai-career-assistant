package com.careerassistant.dto.application;

import java.time.LocalDateTime;

public record ApplicationStatusHistoryResponse(
        String previousStatus,
        String newStatus,
        String changedByName,
        String note,
        LocalDateTime changedAt
) {
}
