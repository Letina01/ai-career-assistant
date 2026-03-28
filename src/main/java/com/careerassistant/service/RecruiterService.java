package com.careerassistant.service;

import com.careerassistant.dto.recruiter.RecruiterApplicantResponse;
import com.careerassistant.dto.recruiter.RecruiterApplicationStatusRequest;
import com.careerassistant.dto.recruiter.RecruiterJobPostingRequest;
import com.careerassistant.dto.recruiter.RecruiterJobPostingResponse;
import com.careerassistant.dto.recruiter.ResumeDownloadPayload;
import java.util.List;

public interface RecruiterService {
    RecruiterJobPostingResponse createJob(RecruiterJobPostingRequest request);
    List<RecruiterJobPostingResponse> getMyJobs();
    List<RecruiterApplicantResponse> getApplicants();
    RecruiterApplicantResponse updateApplicationStatus(Long applicationId, RecruiterApplicationStatusRequest request);
    ResumeDownloadPayload downloadApplicantResume(Long applicationId);
}
