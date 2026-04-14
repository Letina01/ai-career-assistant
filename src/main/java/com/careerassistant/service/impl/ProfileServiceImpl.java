package com.careerassistant.service.impl;

import com.careerassistant.dto.profile.ApplicantProfileRequest;
import com.careerassistant.dto.profile.ApplicantProfileResponse;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.UserAccountRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.OnboardingStateService;
import com.careerassistant.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final CurrentUserService currentUserService;
    private final UserAccountRepository userAccountRepository;
    private final OnboardingStateService onboardingStateService;

    @Override
    public ApplicantProfileResponse getMyProfile() {
        try {
            UserAccount user = currentUserService.getCurrentUser();
//            log.info("Retrieved profile for user: {}", user.getId());
            return toResponse(user);
        } catch (Exception ex) {
            log.error("Failed to get profile: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public ApplicantProfileResponse updateMyProfile(ApplicantProfileRequest request) {
        try {
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
            UserAccount updated = userAccountRepository.save(user);
            log.info("Profile updated for user: {}", user.getId());
            return toResponse(updated);
        } catch (Exception ex) {
            log.error("Failed to update profile: {}", ex.getMessage(), ex);
            throw ex;
        }
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
