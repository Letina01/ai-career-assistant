package com.careerassistant.dto.recruiter;

import java.time.LocalDateTime;

public record RecruiterApplicantResponse(
        Long applicationId,
        Long jobId,
        String jobTitle,
        String company,
        String applicantName,
        String applicantEmail,
        String phone,
        String city,
        String currentRole,
        String currentCompany,
        Integer experienceYears,
        String skills,
        String education,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        String bio,
        Long resumeId,
        Integer atsScore,
        String status,
        LocalDateTime createdAt
) {
}
