package com.careerassistant.service;

import com.careerassistant.dto.ai.ResumeAnalysisResult;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.entity.ApplicationStatus;
import com.careerassistant.entity.JobApplication;
import com.careerassistant.entity.Resume;
import com.careerassistant.entity.UserAccount;
import java.util.List;

public interface EmailService {
    void sendResumeAnalysisReport(Resume resume, ResumeAnalysisResult result);
    void sendJobRecommendations(Resume resume, String preferredRole, String preferredLocation, List<JobRecommendationResponse> jobs);
    void sendApplicationStatusUpdate(JobApplication application, ApplicationStatus previousStatus, ApplicationStatus newStatus);
    void sendPasswordResetToken(UserAccount user, String token);
    void sendWelcomeEmail(UserAccount user);
}
