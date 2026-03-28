package com.careerassistant.dto.application;

import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long applicationId,
        Long jobId,
        String jobTitle,
        String company,
        String applicantName,
        String applicantEmail,
        Long resumeId,
        Integer atsScore,
        String status,
        LocalDateTime createdAt
) {
}
