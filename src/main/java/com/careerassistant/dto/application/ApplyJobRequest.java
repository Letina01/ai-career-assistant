package com.careerassistant.dto.application;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ApplyJobRequest(
    @NotBlank String jobTitle,
    @NotBlank String company,
    String location,
    @NotBlank
    @JsonAlias("jobUrl")
    String applyLink,
    Integer matchScore
) {}
