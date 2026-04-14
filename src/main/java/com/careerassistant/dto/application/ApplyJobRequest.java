package com.careerassistant.dto.application;

public record ApplyJobRequest(
    String jobTitle,
    String company,
    String location,
    String applyLink,
    Integer matchScore
) {}
