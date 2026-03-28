package com.careerassistant.dto.recruiter;

import com.careerassistant.entity.ApplicationStatus;

public record RecruiterApplicationStatusRequest(
        ApplicationStatus status
) {
}
