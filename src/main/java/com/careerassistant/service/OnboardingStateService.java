package com.careerassistant.service;

import com.careerassistant.entity.UserAccount;

public interface OnboardingStateService {
    boolean isProfileComplete(UserAccount user);
    boolean hasUploadedResume(Long userId);
}
