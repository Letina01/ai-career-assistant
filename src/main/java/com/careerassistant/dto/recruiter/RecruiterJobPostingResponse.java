package com.careerassistant.dto.recruiter;

import java.time.LocalDateTime;

public record RecruiterJobPostingResponse(
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
