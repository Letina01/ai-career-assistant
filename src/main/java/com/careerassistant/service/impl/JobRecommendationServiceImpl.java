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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecommendationServiceImpl implements JobRecommendationService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobApiProperties jobApiProperties;
    private final WebClient webClient;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<JobRecommendationResponse> recommendJobs(Long resumeId, String query, String location) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found for resumeId=" + resumeId));
        Long currentUserId = currentUserService.getCurrentUser().getId();
        if (!analysis.getResume().getOwner().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Resume analysis not accessible");
        }
        
        final List<String> skills;
        if (analysis.getExtractedSkills() != null && !analysis.getExtractedSkills().isBlank()) {
            skills = Arrays.stream(analysis.getExtractedSkills().split(","))
                    .map(String::trim)
                    .filter(skill -> !skill.isBlank())
                    .toList();
        } else {
            skills = List.of();
        }

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
    @Transactional
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
    @Transactional(readOnly = true)
    public List<JobRecommendationResponse> getSavedJobs() {
        return savedJobRepository.findByOwnerIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getId()).stream()
                .map(job -> new JobRecommendationResponse(job.getTitle(), job.getCompany(), job.getApplyLink(), job.getMatchScore(), true))
                .toList();
    }

    private List<JobRecommendationResponse> fetchLiveJobs(String query, String location) {
        // Log API configuration
        String apiKey = jobApiProperties.getRapidapi().getKey();
        String apiHost = jobApiProperties.getRapidapi().getHost();
        String apiUrl = jobApiProperties.getRapidapi().getUrl();
        
        log.info("=== JOB API CONFIG ===");
        log.info("API URL: {}", apiUrl);
        log.info("API Host: {}", apiHost);
        log.info("API Key configured: {}", StringUtils.hasText(apiKey));
        if (StringUtils.hasText(apiKey)) {
            log.info("API Key (first 10 chars): {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        }
        
        if (!StringUtils.hasText(apiKey)) {
            log.warn("❌ Job API key NOT configured, returning sample jobs");
            return List.of(
                    new JobRecommendationResponse("Java Spring Boot Developer", "TechNova", "https://example.com/jobs/1", 85, false),
                    new JobRecommendationResponse("React Full Stack Engineer", "CloudAxis", "https://example.com/jobs/2", 75, false),
                    new JobRecommendationResponse("AWS Platform Engineer", "ScaleOps", "https://example.com/jobs/3", 65, false)
            );
        }

        try {
            // Use separate query and location parameters as required by JSearch API
            String uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("query", query)
                    .queryParam("location", location)
                    .queryParam("page", 1)
                    .queryParam("num_pages", 1)
                    .toUriString();
            
            log.info("=== JOB API REQUEST ===");
            log.info("Full URL: {}", uri);
            log.info("Search Query: {} | Location: {}", query, location);
            log.info("Headers: X-RapidAPI-Host={}, X-RapidAPI-Key=***", apiHost);
            
            String response = webClient.get()
                    .uri(uri)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("=== JOB API RESPONSE ===");
            if (response == null) {
                log.error("❌ Response is NULL");
                return List.of();
            }
            
            if (response.isBlank()) {
                log.error("❌ Response is BLANK (empty string)");
                return List.of();
            }
            
            log.info("Response length: {} chars", response.length());
            // Log first 500 chars for debugging
            log.info("Raw response (first 500 chars): {}", response.substring(0, Math.min(500, response.length())));
            
            JsonNode rootNode = objectMapper.readTree(response);
            log.info("Parsed JSON successfully");
            
            // Get field names and log them
            List<String> fieldNames = new ArrayList<>();
            rootNode.fieldNames().forEachRemaining(fieldNames::add);
            log.info("Root node keys: {}", fieldNames.isEmpty() ? "NO FIELDS" : String.join(", ", fieldNames));
            
            // Check API status in response
            JsonNode status = rootNode.path("status");
            if (!status.isMissingNode()) {
                log.info("API Status: {}", status.asText());
            }
            
            // Check if response contains error
            if (rootNode.has("error") && !rootNode.path("error").isNull()) {
                log.error("❌ Job API returned error: {}", rootNode.path("error").asText());
                return List.of();
            }
            
            JsonNode data = rootNode.path("data");
            
            // Handle missing or non-array data
            if (data.isMissingNode()) {
                log.error("❌ Job API response missing 'data' field");
                List<String> availableFields = new ArrayList<>();
                rootNode.fieldNames().forEachRemaining(availableFields::add);
                log.info("Available fields in response: {}", availableFields.isEmpty() ? "NONE" : String.join(", ", availableFields));
                return List.of();
            }
            
            if (!data.isArray()) {
                log.error("❌ Job API 'data' field is not an array, type: {}", data.getNodeType());
                log.info("Data content: {}", data.asText());
                return List.of();
            }
            
            log.info("✅ Data is valid array with {} items", data.size());
            
            // If data is empty, log the full response for analysis
            if (data.size() == 0) {
                log.warn("⚠️ API returned 0 jobs for query: '{}' in location: '{}'", query, location);
                log.warn("Full response for analysis: {}", response);
                log.warn("Tip: Try different search terms or check RapidAPI quota/plan limits");
            }
            
            List<JobRecommendationResponse> jobs = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JsonNode item = data.get(i);
                String title = item.path("job_title").asText("");
                String company = item.path("employer_name").asText("");
                String applyLink = item.path("job_apply_link").asText("");
                String city = item.path("job_city").asText("");
                
                log.debug("Job {}: title='{}', company='{}', city='{}'", i, title, company, city);
                
                // Skip jobs with missing critical fields
                if (title.isBlank() || company.isBlank()) {
                    log.debug("⚠️ Skipping job {} - missing title or company", i);
                    continue;
                }
                
                jobs.add(new JobRecommendationResponse(
                        title,
                        company,
                        applyLink.isBlank() ? "#" : applyLink,
                        0,
                        false
                ));
                log.debug("✅ Added job: {}", title);
            }
            
            log.info("=== JOB API RESULT ===");
            log.info("✅ Successfully processed {} jobs from API", jobs.size());
            return jobs;
            
        } catch (Exception ex) {
            log.error("❌ Failed to fetch jobs from external API", ex);
            log.error("Exception type: {}", ex.getClass().getName());
            log.error("Exception message: {}", ex.getMessage());
            ex.printStackTrace();
            return List.of();
        }
    }

    private Integer calculateMatchScore(List<String> skills, String jobText) {
        if (skills.isEmpty()) {
            return 40; // Base score even with no skills
        }
        String lower = jobText.toLowerCase(Locale.ENGLISH);
        long matches = skills.stream()
                .filter(skill -> {
                    String skillLower = skill.toLowerCase(Locale.ENGLISH);
                    return lower.contains(skillLower) || 
                           lower.contains(skillLower.replace("#", "sharp")) ||
                           lower.contains(skillLower.replace("+", "plus"));
                })
                .count();
        return (int) Math.min(100, 40 + matches * 15);
    }
}

