package com.careerassistant.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ApplicantProfileRequest(
        @Size(max = 255) String fullName,
        @Size(max = 20) String phone,
        @Size(max = 100) String city,
        @Size(max = 255) String preferredRole,
        @Size(max = 255) String preferredLocation,
        @Size(max = 255) String currentRole,
        @Size(max = 255) String currentCompany,
        @Min(0) @Max(50) Integer experienceYears,
        @Min(0) @Max(365) Integer noticePeriodDays,
        @Size(max = 100) String expectedSalary,
        @Size(max = 1024) String bio,
        @Size(max = 1000) String skills,
        @Size(max = 1000) String education,
        @Size(max = 255) String linkedinUrl,
        @Size(max = 255) String githubUrl,
        @Size(max = 255) String portfolioUrl
) {
}
