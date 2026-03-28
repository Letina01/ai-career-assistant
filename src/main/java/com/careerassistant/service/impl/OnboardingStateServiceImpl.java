package com.careerassistant.service.impl;

import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.ResumeRepository;
import com.careerassistant.service.OnboardingStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OnboardingStateServiceImpl implements OnboardingStateService {

    private final ResumeRepository resumeRepository;

    @Override
    public boolean isProfileComplete(UserAccount user) {
        return StringUtils.hasText(user.getFullName())
                && StringUtils.hasText(user.getPhone())
                && StringUtils.hasText(user.getCity())
                && StringUtils.hasText(user.getPreferredRole())
                && StringUtils.hasText(user.getPreferredLocation())
                && StringUtils.hasText(user.getCurrentRole())
                && user.getExperienceYears() != null
                && StringUtils.hasText(user.getSkills())
                && StringUtils.hasText(user.getBio());
    }

    @Override
    public boolean hasUploadedResume(Long userId) {
        return resumeRepository.existsByOwnerId(userId);
    }
}
