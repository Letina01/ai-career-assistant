package com.careerassistant.service.impl;

import com.careerassistant.dto.auth.AuthRequest;
import com.careerassistant.dto.auth.AuthResponse;
import com.careerassistant.dto.auth.RegisterRequest;
import com.careerassistant.dto.auth.RequestPasswordResetRequest;
import com.careerassistant.dto.auth.RequestPasswordResetResponse;
import com.careerassistant.dto.auth.ResetPasswordRequest;
import com.careerassistant.entity.PasswordResetToken;
import com.careerassistant.entity.Role;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.repository.PasswordResetTokenRepository;
import com.careerassistant.repository.UserAccountRepository;
import com.careerassistant.security.JwtService;
import com.careerassistant.service.AuthService;
import com.careerassistant.service.EmailService;
import com.careerassistant.service.OnboardingStateService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OnboardingStateService onboardingStateService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }
        UserAccount user = new UserAccount();
        user.setFullName(request.fullName() == null ? null : request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.APPLICANT : request.role());
        UserAccount saved = userAccountRepository.save(user);
        
        try {
            emailService.sendWelcomeEmail(saved);
        } catch (Exception emailEx) {
            log.warn("Failed to send welcome email: {}", emailEx.getMessage());
        }
        
        return toResponse(saved);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        return toResponse(user);
    }

    @Override
    public RequestPasswordResetResponse requestPasswordReset(RequestPasswordResetRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null) {
            return new RequestPasswordResetResponse(
                    "If the email exists, a reset token has been sent.",
                    null
            );
        }

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetToken(user, token);
        return new RequestPasswordResetResponse(
                "If the email exists, a reset token has been sent.",
                null
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenAndUsedAtIsNull(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }
        UserAccount user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    private AuthResponse toResponse(UserAccount user) {
        String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name(), "userId", user.getId()));
        boolean profileComplete = user.getRole() == Role.RECRUITER || onboardingStateService.isProfileComplete(user);
        boolean resumeUploaded = user.getRole() == Role.RECRUITER || onboardingStateService.hasUploadedResume(user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), profileComplete, resumeUploaded);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        return email.trim().toLowerCase(java.util.Locale.ENGLISH);
    }
}
