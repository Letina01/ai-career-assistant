package com.careerassistant.dto.profile;

public record ApplicantProfileRequest(
        String fullName,
        String phone,
        String city,
        String preferredRole,
        String preferredLocation,
        String currentRole,
        String currentCompany,
        Integer experienceYears,
        Integer noticePeriodDays,
        String expectedSalary,
        String bio,
        String skills,
        String education,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl
) {
}
