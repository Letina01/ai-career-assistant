package com.careerassistant.controller;

import com.careerassistant.dto.auth.AuthRequest;
import com.careerassistant.dto.auth.AuthResponse;
import com.careerassistant.dto.auth.RegisterRequest;
import com.careerassistant.dto.auth.RequestPasswordResetRequest;
import com.careerassistant.dto.auth.RequestPasswordResetResponse;
import com.careerassistant.dto.auth.ResetPasswordRequest;
import com.careerassistant.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/request-password-reset")
    public RequestPasswordResetResponse requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Map.of("message", "Password reset successful");
    }
}
