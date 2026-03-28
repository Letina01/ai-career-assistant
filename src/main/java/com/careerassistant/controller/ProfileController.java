package com.careerassistant.controller;

import com.careerassistant.dto.profile.ApplicantProfileRequest;
import com.careerassistant.dto.profile.ApplicantProfileResponse;
import com.careerassistant.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ApplicantProfileResponse getMyProfile() {
        return profileService.getMyProfile();
    }

    @PutMapping
    public ApplicantProfileResponse updateMyProfile(@RequestBody ApplicantProfileRequest request) {
        return profileService.updateMyProfile(request);
    }
}
