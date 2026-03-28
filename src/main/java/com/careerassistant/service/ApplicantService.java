package com.careerassistant.service;

import com.careerassistant.dto.application.JobApplicationRequest;
import com.careerassistant.dto.application.JobApplicationResponse;
import com.careerassistant.dto.application.ApplicationStatusHistoryResponse;
import com.careerassistant.dto.job.JobPostingResponse;
import java.util.List;

public interface ApplicantService {
    List<JobPostingResponse> getAvailableJobs();
    JobApplicationResponse apply(Long jobId, JobApplicationRequest request);
    List<JobApplicationResponse> getMyApplications();
    List<ApplicationStatusHistoryResponse> getApplicationHistory(Long applicationId);
}
