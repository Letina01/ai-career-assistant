package com.careerassistant.dto.recruiter;

public record RecruiterProfileRequest(
        String fullName,
        String phone,
        String city,
        String recruiterRole,
        String recruiterCompany,
        String companyWebsite,
        String bio
) {
}
