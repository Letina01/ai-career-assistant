package com.careerassistant.dto.dashboard;

import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.resume.ResumeSummaryResponse;
import java.util.List;

public record DashboardResponse(
        List<ResumeSummaryResponse> resumeHistory,
        List<Integer> atsScores,
        List<JobRecommendationResponse> savedJobs
) {
}
