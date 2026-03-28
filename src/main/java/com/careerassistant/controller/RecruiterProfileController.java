package com.careerassistant.controller;

import com.careerassistant.dto.recruiter.RecruiterProfileRequest;
import com.careerassistant.dto.recruiter.RecruiterProfileResponse;
import com.careerassistant.service.RecruiterProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;

    @GetMapping
    public RecruiterProfileResponse getMyProfile() {
        return recruiterProfileService.getMyProfile();
    }

    @PutMapping
    public RecruiterProfileResponse updateMyProfile(@RequestBody RecruiterProfileRequest request) {
        return recruiterProfileService.updateMyProfile(request);
    }
}
