package com.careerassistant.service;

import com.careerassistant.dto.profile.ApplicantProfileRequest;
import com.careerassistant.dto.profile.ApplicantProfileResponse;

public interface ProfileService {
    ApplicantProfileResponse getMyProfile();
    ApplicantProfileResponse updateMyProfile(ApplicantProfileRequest request);
}
