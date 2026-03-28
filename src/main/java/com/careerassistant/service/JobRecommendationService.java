package com.careerassistant.service;

import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.job.SaveJobRequest;
import java.util.List;

public interface JobRecommendationService {
    List<JobRecommendationResponse> recommendJobs(Long resumeId, String query, String location);
    JobRecommendationResponse saveJob(SaveJobRequest request);
    List<JobRecommendationResponse> getSavedJobs();
}
