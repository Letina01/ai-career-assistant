package com.careerassistant.dto.resume;

import java.time.LocalDateTime;

public record ResumeSummaryResponse(
        Long resumeId,
        String candidateName,
        String email,
        String fileName,
        Integer atsScore,
        LocalDateTime createdAt
) {
}
