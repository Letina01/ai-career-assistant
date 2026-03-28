package com.careerassistant.service;

import com.careerassistant.dto.recruiter.RecruiterProfileRequest;
import com.careerassistant.dto.recruiter.RecruiterProfileResponse;

public interface RecruiterProfileService {
    RecruiterProfileResponse getMyProfile();
    RecruiterProfileResponse updateMyProfile(RecruiterProfileRequest request);
}
