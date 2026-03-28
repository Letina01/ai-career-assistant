package com.careerassistant.dto.job;

import java.time.LocalDateTime;

public record JobPostingResponse(
        Long jobId,
        String title,
        String company,
        String location,
        String description,
        String requiredSkills,
        String applyLink,
        String recruiterName,
        LocalDateTime createdAt
) {
}
