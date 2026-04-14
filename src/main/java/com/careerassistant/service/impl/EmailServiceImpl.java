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
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @PostConstruct
    public void init() {
        log.info("=== EMAIL SERVICE INITIALIZED ===");
        log.info("Email Enabled: {}", emailProperties.isEnabled());
        log.info("Email From: {}", emailProperties.getFrom());
        log.info("SMTP Host: {}", mailHost);
        log.info("SMTP Username: {}", mailUsername);
        log.info("JavaMailSender available: {}", javaMailSender != null);
        log.info("===================================");
    }

    @Override
    public void sendResumeAnalysisReport(Resume resume, ResumeAnalysisResult result) {
        if (resume == null || !StringUtils.hasText(resume.getEmail())) {
            log.error("Cannot send resume analysis report: resume or email is null/empty");
            return;
        }

        String subject = "Your Resume Analysis Report";
        if (!emailProperties.isEnabled()) {
            log.debug("Email disabled - skipping resume analysis report for {}", resume.getEmail());
            saveLog(resume, resume.getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        if (!isEmailConfigured()) {
            log.error("Email not configured - skipping resume analysis report for {}", resume.getEmail());
            saveLog(resume, resume.getEmail(), subject, "FAILED", "Email server not configured (MAIL_HOST missing)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = emailProperties.getFrom();
            if (StringUtils.hasText(fromEmail)) {
                message.setFrom(fromEmail);
            }
            message.setTo(resume.getEmail());
            message.setSubject(subject);
            message.setText(buildReportBody(resume, result));
            
            javaMailSender.send(message);
            log.info("Resume analysis report sent successfully to {}", resume.getEmail());
            saveLog(resume, resume.getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.error("Failed to send resume analysis report email to {}: {}", resume.getEmail(), ex.getMessage(), ex);
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
        if (resume == null || !StringUtils.hasText(resume.getEmail())) {
            log.error("Cannot send job recommendations: resume or email is null/empty");
            return;
        }

        String subject = "Recommended Jobs For Your Profile";
        if (!emailProperties.isEnabled()) {
            log.debug("Email disabled - skipping job recommendations for {}", resume.getEmail());
            saveLog(resume, resume.getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        if (!isEmailConfigured()) {
            log.error("Email not configured - skipping job recommendations for {}", resume.getEmail());
            saveLog(resume, resume.getEmail(), subject, "FAILED", "Email server not configured (MAIL_HOST missing)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = emailProperties.getFrom();
            if (StringUtils.hasText(fromEmail)) {
                message.setFrom(fromEmail);
            }
            message.setTo(resume.getEmail());
            message.setSubject(subject);
            message.setText(buildRecommendationsBody(resume, preferredRole, preferredLocation, jobs));
            
            javaMailSender.send(message);
            log.info("Job recommendations sent successfully to {}", resume.getEmail());
            saveLog(resume, resume.getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.error("Failed to send job recommendations email to {}: {}", resume.getEmail(), ex.getMessage(), ex);
            saveLog(resume, resume.getEmail(), subject, "FAILED", ex.getMessage());
        }
    }

    @Override
    public void sendApplicationStatusUpdate(JobApplication application, ApplicationStatus previousStatus, ApplicationStatus newStatus) {
        if (application == null || application.getApplicant() == null || !StringUtils.hasText(application.getApplicant().getEmail())) {
            log.error("Cannot send application status update: application or applicant email is null/empty");
            return;
        }

        String subject = "Your Job Application Status Updated";
        String applicantEmail = application.getApplicant().getEmail();
        
        if (!emailProperties.isEnabled()) {
            log.debug("Email disabled - skipping application status update for {}", applicantEmail);
            saveLog(application.getResume(), applicantEmail, subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        if (!isEmailConfigured()) {
            log.error("Email not configured - skipping application status update for {}", applicantEmail);
            saveLog(application.getResume(), applicantEmail, subject, "FAILED", "Email server not configured (MAIL_HOST missing)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = emailProperties.getFrom();
            if (StringUtils.hasText(fromEmail)) {
                message.setFrom(fromEmail);
            }
            message.setTo(applicantEmail);
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
            log.info("Application status update sent successfully to {}", applicantEmail);
            saveLog(application.getResume(), applicantEmail, subject, "SENT", null);
        } catch (Exception ex) {
            log.error("Failed to send application status email to {}: {}", applicantEmail, ex.getMessage(), ex);
            saveLog(application.getResume(), applicantEmail, subject, "FAILED", ex.getMessage());
        }
    }

    @Override
    public void sendPasswordResetToken(UserAccount user, String token) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            log.error("Cannot send password reset token: user or email is null/empty");
            return;
        }

        String subject = "Password Reset Token";
        String userEmail = user.getEmail();
        
        if (!emailProperties.isEnabled()) {
            log.debug("Email disabled - skipping password reset token for {}", userEmail);
            saveLog(null, userEmail, subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        if (!isEmailConfigured()) {
            log.error("Email not configured - skipping password reset token for {}", userEmail);
            saveLog(null, userEmail, subject, "FAILED", "Email server not configured (MAIL_HOST missing)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = emailProperties.getFrom();
            if (StringUtils.hasText(fromEmail)) {
                message.setFrom(fromEmail);
            }
            message.setTo(userEmail);
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
            log.info("Password reset token sent successfully to {}", userEmail);
            saveLog(null, userEmail, subject, "SENT", null);
        } catch (Exception ex) {
            log.error("Failed to send password reset token email to {}: {}", userEmail, ex.getMessage(), ex);
            saveLog(null, userEmail, subject, "FAILED", ex.getMessage());
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

    private boolean isEmailConfigured() {
        return StringUtils.hasText(mailHost) && StringUtils.hasText(mailUsername);
    }

    private void saveLog(Resume resume, String recipient, String subject, String status, String errorMessage) {
        try {
            EmailLog logEntry = new EmailLog();
            logEntry.setResume(resume);
            logEntry.setRecipientEmail(recipient);
            logEntry.setSubject(subject);
            logEntry.setStatus(status);
            logEntry.setErrorMessage(errorMessage);
            emailLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.error("Failed to save email log for {}: {}", recipient, ex.getMessage(), ex);
        }
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

    @Override
    public void sendWelcomeEmail(UserAccount user) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            log.error("Cannot send welcome email: user or email is null/empty");
            return;
        }

        String subject = "Welcome to AI Career Assistant";
        if (!emailProperties.isEnabled()) {
            log.debug("Email disabled - skipping welcome email for {}", user.getEmail());
            saveLog(null, user.getEmail(), subject, "SKIPPED", "Email sending is disabled");
            return;
        }

        if (!isEmailConfigured()) {
            log.error("Email not configured - skipping welcome email for {}", user.getEmail());
            saveLog(null, user.getEmail(), subject, "FAILED", "Email server not configured");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String fromEmail = emailProperties.getFrom();
            if (StringUtils.hasText(fromEmail)) {
                message.setFrom(fromEmail);
            }
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(buildWelcomeEmailBody(user));
            
            javaMailSender.send(message);
            log.info("Welcome email sent successfully to {}", user.getEmail());
            saveLog(null, user.getEmail(), subject, "SENT", null);
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), ex.getMessage(), ex);
            saveLog(null, user.getEmail(), subject, "FAILED", ex.getMessage());
        }
    }

    private String buildWelcomeEmailBody(UserAccount user) {
        String roleMessage = user.getRole() == com.careerassistant.entity.Role.RECRUITER
                ? "As a Recruiter, you can post jobs, review applicants, and manage your hiring pipeline."
                : "As an Applicant, you can upload your resume, get AI-powered analysis, and find your dream job.";

        return """
                Hi %s,

                Welcome to AI Career Assistant!

                Your account has been successfully created.
                Role: %s

                %s

                Here's what you can do next:

                For Applicants:
                - Complete your profile
                - Upload your resume for AI analysis
                - Get job recommendations
                - Prepare for interviews
                - Analyze skill gaps

                For Recruiters:
                - Post new job openings
                - Review applicant profiles
                - Shortlist candidates
                - Manage applications

                Best regards,
                AI Career Assistant Team
                """.formatted(
                user.getFullName() != null ? user.getFullName() : "User",
                user.getRole().name(),
                roleMessage
        );
    }
}
