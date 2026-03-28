package com.careerassistant.service.impl;

import com.careerassistant.config.properties.JobApiProperties;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.dto.job.SaveJobRequest;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.entity.SavedJob;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.repository.SavedJobRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.JobRecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class JobRecommendationServiceImpl implements JobRecommendationService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobApiProperties jobApiProperties;
    private final WebClient webClient;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<JobRecommendationResponse> recommendJobs(Long resumeId, String query, String location) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found for resumeId=" + resumeId));
        Long currentUserId = currentUserService.getCurrentUser().getId();
        if (!analysis.getResume().getOwner().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Resume analysis not accessible");
        }
        List<String> skills = Arrays.stream(analysis.getExtractedSkills().split(","))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();

        return fetchLiveJobs(query, location).stream()
                .map(job -> new JobRecommendationResponse(
                        job.title(),
                        job.company(),
                        job.applyLink(),
                        calculateMatchScore(skills, job.title() + " " + job.company()),
                        false
                ))
                .sorted((left, right) -> right.matchScore().compareTo(left.matchScore()))
                .toList();
    }

    @Override
    public JobRecommendationResponse saveJob(SaveJobRequest request) {
        SavedJob job = new SavedJob();
        job.setOwner(currentUserService.getCurrentUser());
        job.setTitle(request.title());
        job.setCompany(request.company());
        job.setApplyLink(request.applyLink());
        job.setMatchScore(request.matchScore());
        SavedJob saved = savedJobRepository.save(job);
        return new JobRecommendationResponse(saved.getTitle(), saved.getCompany(), saved.getApplyLink(), saved.getMatchScore(), true);
    }

    @Override
    public List<JobRecommendationResponse> getSavedJobs() {
        return savedJobRepository.findByOwnerIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId()).stream()
                .map(job -> new JobRecommendationResponse(job.getTitle(), job.getCompany(), job.getApplyLink(), job.getMatchScore(), true))
                .toList();
    }

    private List<JobRecommendationResponse> fetchLiveJobs(String query, String location) {
        if (!StringUtils.hasText(jobApiProperties.getRapidapi().getKey())) {
            return List.of(
                    new JobRecommendationResponse("Java Spring Boot Developer", "TechNova", "https://example.com/jobs/1", 0, false),
                    new JobRecommendationResponse("React Full Stack Engineer", "CloudAxis", "https://example.com/jobs/2", 0, false),
                    new JobRecommendationResponse("AWS Platform Engineer", "ScaleOps", "https://example.com/jobs/3", 0, false)
            );
        }

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(jobApiProperties.getRapidapi().getUrl())
                    .queryParam("query", query + " in " + location)
                    .queryParam("page", 1)
                    .queryParam("num_pages", 1)
                    .toUriString();
            String response = webClient.get()
                    .uri(uri)
                    .header("X-RapidAPI-Key", jobApiProperties.getRapidapi().getKey())
                    .header("X-RapidAPI-Host", jobApiProperties.getRapidapi().getHost())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode data = objectMapper.readTree(response).path("data");
            List<JobRecommendationResponse> jobs = new ArrayList<>();
            for (JsonNode item : data) {
                jobs.add(new JobRecommendationResponse(
                        item.path("job_title").asText("Unknown role"),
                        item.path("employer_name").asText("Unknown company"),
                        item.path("job_apply_link").asText("#"),
                        0,
                        false
                ));
            }
            return jobs;
        } catch (Exception ex) {
            return List.of(new JobRecommendationResponse("Backend Engineer", "Fallback Labs", "https://example.com/jobs/fallback", 0, false));
        }
    }

    private Integer calculateMatchScore(List<String> skills, String jobText) {
        String lower = jobText.toLowerCase(Locale.ENGLISH);
        long matches = skills.stream().filter(skill -> lower.contains(skill.toLowerCase(Locale.ENGLISH))).count();
        return (int) Math.min(100, 40 + matches * 15);
    }
}
