package com.careerassistant.service.impl;

import com.careerassistant.config.properties.EmailProperties;
import com.careerassistant.dto.ai.ResumeAnalysisResult;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.entity.ApplicationStatus;
import com.careerassistant.entity.EmailLog;
import com.careerassistant.entity.JobApplication;
import com.careerassistant.entity.Resume;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.EmailLogRepository;
import com.careerassistant.service.EmailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;
    private final EmailLogRepository emailLogRepository;

    @Override
    public void sendResumeAnalysisReport(Resume resume, ResumeAnalysisResult result) {
        String subject = "Your Resume Analysis Report";
        if (!emailProperties.isEnabled()) {
            saveLog(resume, resume.getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(emailProperties.getFrom())) {
                message.setFrom(emailProperties.getFrom());
            }
            message.setTo(resume.getEmail());
            message.setSubject(subject);
            message.setText(buildReportBody(resume, result));
            javaMailSender.send(message);
            saveLog(resume, resume.getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.warn("Failed to send resume analysis report email: {}", ex.getMessage());
            saveLog(resume, resume.getEmail(), subject, "FAILED", ex.getMessage());
        }
    }

    @Override
    public void sendJobRecommendations(
            Resume resume,
            String preferredRole,
            String preferredLocation,
            List<JobRecommendationResponse> jobs
    ) {
        String subject = "Recommended Jobs For Your Profile";
        if (!emailProperties.isEnabled()) {
            saveLog(resume, resume.getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(emailProperties.getFrom())) {
                message.setFrom(emailProperties.getFrom());
            }
            message.setTo(resume.getEmail());
            message.setSubject(subject);
            message.setText(buildRecommendationsBody(resume, preferredRole, preferredLocation, jobs));
            javaMailSender.send(message);
            saveLog(resume, resume.getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.warn("Failed to send job recommendations email: {}", ex.getMessage());
            saveLog(resume, resume.getEmail(), subject, "FAILED", ex.getMessage());
        }
    }

    @Override
    public void sendApplicationStatusUpdate(JobApplication application, ApplicationStatus previousStatus, ApplicationStatus newStatus) {
        String subject = "Your Job Application Status Updated";
        if (!emailProperties.isEnabled()) {
            saveLog(application.getResume(), application.getApplicant().getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(emailProperties.getFrom())) {
                message.setFrom(emailProperties.getFrom());
            }
            message.setTo(application.getApplicant().getEmail());
            message.setSubject(subject);
            message.setText("""
                    Hi %s,

                    Your application for %s at %s has been updated.
                    Previous status: %s
                    Current status: %s

                    Thanks,
                    AI Career Assistant
                    """.formatted(
                    application.getApplicant().getFullName(),
                    application.getJobPosting().getTitle(),
                    application.getJobPosting().getCompany(),
                    previousStatus == null ? "N/A" : previousStatus.name(),
                    newStatus.name()
            ));
            javaMailSender.send(message);
            saveLog(application.getResume(), application.getApplicant().getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.warn("Failed to send application status email: {}", ex.getMessage());
            saveLog(application.getResume(), application.getApplicant().getEmail(), subject, "FAILED", ex.getMessage());
        }
    }

    @Override
    public void sendPasswordResetToken(UserAccount user, String token) {
        String subject = "Password Reset Token";
        if (!emailProperties.isEnabled()) {
            saveLog(null, user.getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(emailProperties.getFrom())) {
                message.setFrom(emailProperties.getFrom());
            }
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText("""
                    Hi %s,

                    Use this one-time token to reset your password:
                    %s

                    This token expires in 15 minutes.

                    Thanks,
                    AI Career Assistant
                    """.formatted(user.getFullName(), token));
            javaMailSender.send(message);
            saveLog(null, user.getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.warn("Failed to send password reset token email: {}", ex.getMessage());
            saveLog(null, user.getEmail(), subject, "FAILED", ex.getMessage());
        }
    }

    private String buildReportBody(Resume resume, ResumeAnalysisResult result) {
        return """
                Hi %s,

                Your resume analysis is complete.

                ATS Score: %d
                Extracted Skills: %s
                Missing Skills: %s
                Suggestions:
                - %s

                Thanks,
                AI Career Assistant
                """.formatted(
                resume.getCandidateName(),
                result.atsScore(),
                String.join(", ", result.extractedSkills()),
                String.join(", ", result.missingSkills()),
                String.join("\n- ", result.suggestions())
        );
    }

    private void saveLog(Resume resume, String recipient, String subject, String status, String errorMessage) {
        EmailLog logEntry = new EmailLog();
        logEntry.setResume(resume);
        logEntry.setRecipientEmail(recipient);
        logEntry.setSubject(subject);
        logEntry.setStatus(status);
        logEntry.setErrorMessage(errorMessage);
        emailLogRepository.save(logEntry);
    }

    private String buildRecommendationsBody(
            Resume resume,
            String preferredRole,
            String preferredLocation,
            List<JobRecommendationResponse> jobs
    ) {
        List<JobRecommendationResponse> topJobs = jobs.stream().limit(5).toList();
        StringBuilder bulletJobs = new StringBuilder();
        for (int index = 0; index < topJobs.size(); index++) {
            JobRecommendationResponse job = topJobs.get(index);
            bulletJobs.append(index + 1)
                    .append(". ")
                    .append(job.title())
                    .append(" at ")
                    .append(job.company())
                    .append(" (Match: ")
                    .append(job.matchScore())
                    .append("%)")
                    .append("\n   Apply: ")
                    .append(job.applyLink())
                    .append("\n");
        }
        if (bulletJobs.isEmpty()) {
            bulletJobs.append("No recommendations available right now.");
        }

        return """
                Hi %s,

                Based on your profile preferences and latest resume, here are suggested jobs.
                Preferred role: %s
                Preferred location: %s

                %s

                Thanks,
                AI Career Assistant
                """.formatted(
                resume.getCandidateName(),
                StringUtils.hasText(preferredRole) ? preferredRole : "Not specified",
                StringUtils.hasText(preferredLocation) ? preferredLocation : "Not specified",
                bulletJobs
        );
    }
}
