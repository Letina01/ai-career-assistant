package com.careerassistant.dto.chat;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        Long sessionId,
        String title,
        String intent,
        String reply,
        List<ChatMessageDto> history,
        LocalDateTime updatedAt
) {
    public record ChatMessageDto(
            String role,
            String intent,
            String content,
            LocalDateTime createdAt
    ) {
    }
}
