package com.careerassistant.service.impl;

import com.careerassistant.config.properties.AiProperties;
import com.careerassistant.dto.ai.ResumeAnalysisResult;
import com.careerassistant.dto.interview.InterviewPreparationResponse;
import com.careerassistant.dto.resumeimprove.ResumeImprovementResponse;
import com.careerassistant.dto.skillgap.SkillGapResponse;
import com.careerassistant.exception.ExternalServiceException;
import com.careerassistant.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private static final List<String> COMMON_SKILLS = List.of(
            "Java", "Spring Boot", "React", "AWS", "Docker", "Kubernetes",
            "SQL", "REST APIs", "Git", "CI/CD", "Python", "Microservices"
    );

    private final WebClient webClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResumeAnalysisResult analyzeResume(String resumeText) {
        List<String> extractedSkills = inferSkills(resumeText);
        int atsScore = Math.min(98, 35 + extractedSkills.size() * 6);
        List<String> missingSkills = COMMON_SKILLS.stream()
                .filter(skill -> extractedSkills.stream().noneMatch(found -> found.equalsIgnoreCase(skill)))
                .limit(5)
                .toList();
        List<String> suggestions = List.of(
                "Add measurable achievements to each recent project.",
                "Expand role-aligned keywords in summary and experience sections.",
                "Highlight cloud, deployment, and API ownership explicitly.",
                "Shorten generic content and prioritize impact statements."
        );

        if (!hasApiKey()) {
            return new ResumeAnalysisResult(extractedSkills, atsScore, missingSkills, suggestions);
        }

        try {
            String reply = callProvider("""
                    Analyze the following resume and return strict JSON with keys:
                    extractedSkills, atsScore, missingSkills, suggestions.

                    Resume:
                    %s
                    """.formatted(resumeText));
            JsonNode json = objectMapper.readTree(reply);
            return new ResumeAnalysisResult(
                    readList(json, "extractedSkills", extractedSkills),
                    json.path("atsScore").asInt(atsScore),
                    readList(json, "missingSkills", missingSkills),
                    readList(json, "suggestions", suggestions)
            );
        } catch (Exception ex) {
            log.warn("AI resume analysis failed, using deterministic fallback: {}", ex.getMessage());
            return new ResumeAnalysisResult(extractedSkills, atsScore, missingSkills, suggestions);
        }
    }

    @Override
    public String chat(String prompt, List<String> history) {
        if (hasApiKey()) {
            try {
                return callProvider("History:\n" + String.join("\n", history) + "\n\nUser:\n" + prompt);
            } catch (Exception ex) {
                log.warn("AI chat call failed, using local fallback response: {}", ex.getMessage());
            }
        }
        String lower = prompt.toLowerCase(Locale.ENGLISH);
        if (lower.contains("resume")) {
            return "Upload a resume first, then I can analyze ATS score, missing keywords, and rewrite weak sections.";
        }
        if (lower.contains("job")) {
            return "I can rank live jobs against your extracted skills and return title, company, and apply link.";
        }
        if (lower.contains("aws")) {
            return "For AWS interviews, prepare IAM, VPC, S3, EC2, Lambda, CloudWatch, and architecture tradeoffs.";
        }
        return "Ask about resume analysis, job suggestions, interview prep, skill gaps, or resume rewriting.";
    }

    @Override
    public InterviewPreparationResponse generateInterviewPreparation(String role, String focusArea) {
        List<InterviewPreparationResponse.QuestionAnswer> fallbackQuestions = List.of(
                new InterviewPreparationResponse.QuestionAnswer(
                        "Tell me about a project where you solved a high-impact " + role + " problem.",
                        "Use STAR: context, constraints, your technical choices, measurable impact, and what you would improve."
                ),
                new InterviewPreparationResponse.QuestionAnswer(
                        "How would you approach " + focusArea + " for this role?",
                        "Break into requirements, architecture, tradeoffs, failure scenarios, and monitoring."
                ),
                new InterviewPreparationResponse.QuestionAnswer(
                        "What mistakes do candidates make in " + role + " interviews?",
                        "Giving generic answers, skipping tradeoffs, weak fundamentals, and not quantifying past impact."
                ),
                new InterviewPreparationResponse.QuestionAnswer(
                        "How do you validate your technical decisions under constraints?",
                        "Explain assumptions, compare alternatives, estimate cost/performance, and define rollback strategy."
                ),
                new InterviewPreparationResponse.QuestionAnswer(
                        "How would you explain a complex concept to non-technical stakeholders?",
                        "Use simple language, visuals, business impact, risks, and a clear decision recommendation."
                )
        );
        List<String> fallbackRoadmap = List.of(
                "Week 1: Master role-specific fundamentals and revise top interview topics.",
                "Week 2: Practice 20 targeted questions on " + role + " and " + focusArea + ".",
                "Week 3: Run 3 mock interviews and improve weak answer patterns.",
                "Week 4: Prepare company-specific stories, metrics, and follow-up questions."
        );

        if (!hasApiKey()) {
            return new InterviewPreparationResponse(role, fallbackQuestions, fallbackRoadmap);
        }

        try {
            String reply = callProvider("""
                    You are an expert interview coach.
                    Generate practical, real-world interview preparation in STRICT JSON.
                    Return only JSON with keys:
                    role, questionAnswers, roadmap
                    - role: string
                    - questionAnswers: array of 5 objects with keys question and answer
                    - roadmap: array of 4 to 6 concise steps

                    Target role: %s
                    Candidate context and focus:
                    %s
                    """.formatted(role, focusArea));
            JsonNode json = objectMapper.readTree(reply);
            String resolvedRole = json.path("role").asText(role);
            List<InterviewPreparationResponse.QuestionAnswer> questionAnswers =
                    readQuestionAnswers(json, fallbackQuestions);
            List<String> roadmap = readList(json, "roadmap", fallbackRoadmap);
            return new InterviewPreparationResponse(resolvedRole, questionAnswers, roadmap);
        } catch (Exception ex) {
            log.warn("AI interview preparation failed, using deterministic fallback: {}", ex.getMessage());
            return new InterviewPreparationResponse(role, fallbackQuestions, fallbackRoadmap);
        }
    }

    @Override
    public SkillGapResponse generateSkillGap(String targetRole, List<String> currentSkills) {
        List<String> missingSkills = COMMON_SKILLS.stream()
                .filter(skill -> currentSkills.stream().noneMatch(existing -> existing.equalsIgnoreCase(skill)))
                .limit(6)
                .toList();
        return new SkillGapResponse(
                targetRole,
                currentSkills,
                missingSkills,
                List.of(
                        "Week 1-2: map " + targetRole + " expectations and refresh core concepts.",
                        "Week 3-4: build one project covering " + String.join(", ", missingSkills.stream().limit(3).toList()) + ".",
                        "Week 5-6: practice interview questions and deploy your project.",
                        "Week 7+: tailor resume and start targeted applications."
                )
        );
    }

    @Override
    public ResumeImprovementResponse improveResumeSection(String sectionName, String originalContent) {
        String improved = "Improved " + sectionName + ": " + originalContent
                .replace("worked on", "delivered")
                .replace("responsible for", "owned")
                + " Added measurable impact and stronger action verbs.";
        return new ResumeImprovementResponse(sectionName, originalContent, improved);
    }

    private List<String> inferSkills(String text) {
        String lower = text.toLowerCase(Locale.ENGLISH);
        Set<String> skills = new LinkedHashSet<>();
        for (String skill : COMMON_SKILLS) {
            if (lower.contains(skill.toLowerCase(Locale.ENGLISH))) {
                skills.add(skill);
            }
        }
        if (skills.isEmpty()) {
            skills.add("Communication");
            skills.add("Problem Solving");
            skills.add("Teamwork");
        }
        return new ArrayList<>(skills);
    }

    private boolean hasApiKey() {
        return hasOpenAiKey() || hasGeminiKey();
    }

    private String callProvider(String prompt) {
        String preferred = resolvePreferredProvider();
        try {
            return "gemini".equals(preferred) ? callGemini(prompt) : callOpenAi(prompt);
        } catch (ExternalServiceException ex) {
            String alternate = "gemini".equals(preferred) ? "openai" : "gemini";
            if (!providerHasKey(alternate)) {
                throw ex;
            }
            log.warn("Primary AI provider '{}' failed ({}), retrying with '{}'", preferred, ex.getMessage(), alternate);
            return "gemini".equals(alternate) ? callGemini(prompt) : callOpenAi(prompt);
        }
    }

    private String callOpenAi(String prompt) {
        try {
            String response = webClient.post()
                    .uri(aiProperties.getOpenai().getUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getOpenai().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"model":"%s","messages":[{"role":"system","content":"You are a precise career assistant."},{"role":"user","content":%s}],"temperature":0.4}
                            """.formatted(aiProperties.getOpenai().getModel(), toJson(prompt)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return objectMapper.readTree(response).path("choices").path(0).path("message").path("content").asText();
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to call OpenAI provider: " + ex.getMessage());
        }
    }

    private String callGemini(String prompt) {
        try {
            String response = webClient.post()
                    .uri(aiProperties.getGemini().getUrl() + "/" + aiProperties.getGemini().getModel() + ":generateContent?key=" + aiProperties.getGemini().getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"contents":[{"parts":[{"text":%s}]}]}
                            """.formatted(toJson(prompt)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return objectMapper.readTree(response).path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to call Gemini provider: " + ex.getMessage());
        }
    }

    private List<String> readList(JsonNode json, String field, List<String> fallback) {
        JsonNode node = json.path(field);
        if (!node.isArray()) {
            return fallback;
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asText()));
        return values.isEmpty() ? fallback : values;
    }

    private List<InterviewPreparationResponse.QuestionAnswer> readQuestionAnswers(
            JsonNode json,
            List<InterviewPreparationResponse.QuestionAnswer> fallback
    ) {
        JsonNode node = json.path("questionAnswers");
        if (!node.isArray()) {
            return fallback;
        }
        List<InterviewPreparationResponse.QuestionAnswer> values = new ArrayList<>();
        node.forEach(item -> {
            String question = item.path("question").asText();
            String answer = item.path("answer").asText();
            if (StringUtils.hasText(question) && StringUtils.hasText(answer)) {
                values.add(new InterviewPreparationResponse.QuestionAnswer(question, answer));
            }
        });
        return values.isEmpty() ? fallback : values;
    }

    private String toJson(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to serialize prompt");
        }
    }

    private String resolvePreferredProvider() {
        String configured = aiProperties.getProvider() == null
                ? ""
                : aiProperties.getProvider().toLowerCase(Locale.ENGLISH);
        if ("gemini".equals(configured) && hasGeminiKey()) {
            return "gemini";
        }
        if ("openai".equals(configured) && hasOpenAiKey()) {
            return "openai";
        }
        if (hasOpenAiKey()) {
            return "openai";
        }
        if (hasGeminiKey()) {
            return "gemini";
        }
        return "openai";
    }

    private boolean providerHasKey(String provider) {
        return "gemini".equals(provider) ? hasGeminiKey() : hasOpenAiKey();
    }

    private boolean hasOpenAiKey() {
        return StringUtils.hasText(aiProperties.getOpenai().getApiKey());
    }

    private boolean hasGeminiKey() {
        return StringUtils.hasText(aiProperties.getGemini().getApiKey());
    }
}
