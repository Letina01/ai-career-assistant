package com.careerassistant.dto.application;

import java.time.LocalDateTime;

public record ExternalJobApplicationResponse(
    Long applicationId,
    String jobTitle,
    String company,
    String location,
    String applyLink,
    Integer matchScore,
    String applicationStatus,
    LocalDateTime appliedDate
) {}
