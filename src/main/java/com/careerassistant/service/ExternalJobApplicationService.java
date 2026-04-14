package com.careerassistant.service;

import com.careerassistant.dto.application.ApplyJobRequest;
import com.careerassistant.dto.application.ExternalJobApplicationResponse;
import java.util.List;

public interface ExternalJobApplicationService {

    /**
     * Apply to an external job
     * @param request Job application details
     * @return Application response with ID and details
     */
    ExternalJobApplicationResponse applyToJob(ApplyJobRequest request);

    /**
     * Get all applications for current user
     * @return List of applications
     */
    List<ExternalJobApplicationResponse> getMyApplications();

    /**
     * Get application count for current user
     * @return Total applications
     */
    long getTotalApplicationCount();

    /**
     * Check if user already applied to a job
     * @param jobTitle Job title
     * @param company Company name
     * @return true if already applied
     */
    boolean hasApplied(String jobTitle, String company);
}
