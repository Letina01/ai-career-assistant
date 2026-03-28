package com.careerassistant.service.impl;

import com.careerassistant.dto.recruiter.RecruiterProfileRequest;
import com.careerassistant.dto.recruiter.RecruiterProfileResponse;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.UserAccountRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.RecruiterProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterProfileServiceImpl implements RecruiterProfileService {

    private final CurrentUserService currentUserService;
    private final UserAccountRepository userAccountRepository;

    @Override
    public RecruiterProfileResponse getMyProfile() {
        return toResponse(currentUserService.getCurrentUser());
    }

    @Override
    public RecruiterProfileResponse updateMyProfile(RecruiterProfileRequest request) {
        UserAccount user = currentUserService.getCurrentUser();
        user.setFullName(valueOrDefault(request.fullName(), user.getFullName()));
        user.setPhone(request.phone());
        user.setCity(request.city());
        user.setRecruiterRole(request.recruiterRole());
        user.setRecruiterCompany(request.recruiterCompany());
        user.setCompanyWebsite(request.companyWebsite());
        user.setBio(request.bio());
        return toResponse(userAccountRepository.save(user));
    }

    private RecruiterProfileResponse toResponse(UserAccount user) {
        return new RecruiterProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getCity(),
                user.getRecruiterRole(),
                user.getRecruiterCompany(),
                user.getCompanyWebsite(),
                user.getBio(),
                user.getRole().name()
        );
    }

    private String valueOrDefault(String newValue, String fallback) {
        return newValue == null || newValue.isBlank() ? fallback : newValue;
    }
}
