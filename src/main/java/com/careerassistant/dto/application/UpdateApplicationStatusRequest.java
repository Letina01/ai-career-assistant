package com.careerassistant.dto.application;

import com.careerassistant.entity.ApplicationStatus;

public record UpdateApplicationStatusRequest(
        ApplicationStatus status
) {
}
