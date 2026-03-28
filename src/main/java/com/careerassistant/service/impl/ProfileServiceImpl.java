package com.careerassistant.service.impl;

import com.careerassistant.dto.profile.ApplicantProfileRequest;
import com.careerassistant.dto.profile.ApplicantProfileResponse;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.UserAccountRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.OnboardingStateService;
import com.careerassistant.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPLICANT')")
public class ProfileServiceImpl implements ProfileService {

    private final CurrentUserService currentUserService;
    private final UserAccountRepository userAccountRepository;
    private final OnboardingStateService onboardingStateService;

    @Override
    public ApplicantProfileResponse getMyProfile() {
        return toResponse(currentUserService.getCurrentUser());
    }

    @Override
    public ApplicantProfileResponse updateMyProfile(ApplicantProfileRequest request) {
        UserAccount user = currentUserService.getCurrentUser();
        user.setFullName(valueOrDefault(request.fullName(), user.getFullName()));
        user.setPhone(request.phone());
        user.setCity(request.city());
        user.setPreferredRole(request.preferredRole());
        user.setPreferredLocation(request.preferredLocation());
        user.setCurrentRole(request.currentRole());
        user.setCurrentCompany(request.currentCompany());
        user.setExperienceYears(request.experienceYears());
        user.setNoticePeriodDays(request.noticePeriodDays());
        user.setExpectedSalary(request.expectedSalary());
        user.setBio(request.bio());
        user.setSkills(request.skills());
        user.setEducation(request.education());
        user.setLinkedinUrl(request.linkedinUrl());
        user.setGithubUrl(request.githubUrl());
        user.setPortfolioUrl(request.portfolioUrl());
        return toResponse(userAccountRepository.save(user));
    }

    private ApplicantProfileResponse toResponse(UserAccount user) {
        return new ApplicantProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getCity(),
                user.getPreferredRole(),
                user.getPreferredLocation(),
                user.getCurrentRole(),
                user.getCurrentCompany(),
                user.getExperienceYears(),
                user.getNoticePeriodDays(),
                user.getExpectedSalary(),
                user.getBio(),
                user.getSkills(),
                user.getEducation(),
                user.getLinkedinUrl(),
                user.getGithubUrl(),
                user.getPortfolioUrl(),
                user.getRole().name(),
                onboardingStateService.isProfileComplete(user),
                onboardingStateService.hasUploadedResume(user.getId())
        );
    }

    private String valueOrDefault(String newValue, String fallback) {
        return newValue == null || newValue.isBlank() ? fallback : newValue;
    }
}
