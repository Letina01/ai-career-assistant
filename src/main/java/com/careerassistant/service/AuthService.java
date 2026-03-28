package com.careerassistant.service;

import com.careerassistant.dto.auth.AuthRequest;
import com.careerassistant.dto.auth.AuthResponse;
import com.careerassistant.dto.auth.RegisterRequest;
import com.careerassistant.dto.auth.RequestPasswordResetRequest;
import com.careerassistant.dto.auth.RequestPasswordResetResponse;
import com.careerassistant.dto.auth.ResetPasswordRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    RequestPasswordResetResponse requestPasswordReset(RequestPasswordResetRequest request);
    void resetPassword(ResetPasswordRequest request);
}
