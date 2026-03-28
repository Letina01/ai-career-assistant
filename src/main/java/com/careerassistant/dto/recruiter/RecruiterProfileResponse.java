package com.careerassistant.dto.recruiter;

public record RecruiterProfileResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        String city,
        String recruiterRole,
        String recruiterCompany,
        String companyWebsite,
        String bio,
        String role
) {
}
