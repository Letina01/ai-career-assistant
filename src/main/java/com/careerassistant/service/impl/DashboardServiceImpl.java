package com.careerassistant.service.impl;

import com.careerassistant.dto.dashboard.DashboardResponse;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.resume.ResumeSummaryResponse;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.repository.ResumeRepository;
import com.careerassistant.repository.SavedJobRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final SavedJobRepository savedJobRepository;
    private final CurrentUserService currentUserService;

    @Override
    public DashboardResponse getDashboard() {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        List<ResumeSummaryResponse> resumeHistory = resumeRepository.findByOwnerIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(resume -> new ResumeSummaryResponse(
                        resume.getId(),
                        resume.getCandidateName(),
                        resume.getEmail(),
                        resume.getFileName(),
                        resumeAnalysisRepository.findByResumeId(resume.getId()).map(ResumeAnalysis::getAtsScore).orElse(null),
                        resume.getCreatedAt()
                ))
                .toList();

        List<Integer> atsScores = resumeHistory.stream()
                .map(ResumeSummaryResponse::atsScore)
                .filter(score -> score != null)
                .toList();

        List<JobRecommendationResponse> savedJobs = savedJobRepository.findByOwnerIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(job -> new JobRecommendationResponse(job.getTitle(), job.getCompany(), job.getApplyLink(), job.getMatchScore(), true))
                .toList();

        return new DashboardResponse(resumeHistory, atsScores, savedJobs);
    }
}
