package com.careerassistant.service.impl;

import com.careerassistant.dto.chat.ChatResponse;
import com.careerassistant.dto.interview.InterviewPreparationResponse;
import com.careerassistant.dto.job.JobRecommendationResponse;
import com.careerassistant.entity.ChatIntent;
import com.careerassistant.entity.ChatMessage;
import com.careerassistant.entity.ChatSession;
import com.careerassistant.entity.ResumeAnalysis;
import com.careerassistant.entity.UserAccount;
import com.careerassistant.exception.ResourceNotFoundException;
import com.careerassistant.repository.ChatMessageRepository;
import com.careerassistant.repository.ChatSessionRepository;
import com.careerassistant.repository.ResumeAnalysisRepository;
import com.careerassistant.security.CurrentUserService;
import com.careerassistant.service.AiService;
import com.careerassistant.service.ChatService;
import com.careerassistant.service.IntentDetectionService;
import com.careerassistant.service.JobRecommendationService;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AiService aiService;
    private final JobRecommendationService jobRecommendationService;
    private final IntentDetectionService intentDetectionService;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public ChatResponse chat(Long sessionId, String message) {
        ChatSession session = sessionId == null ? createSession(message)
                : chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!session.getOwner().getId().equals(currentUserService.getCurrentUser().getId())) {
            throw new ResourceNotFoundException("Chat session not accessible");
        }
        ChatIntent intent = intentDetectionService.detectIntent(message);
        UserAccount user = currentUserService.getCurrentUser();

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSession(session);
        userMessage.setRole("user");
        userMessage.setIntent(intent);
        userMessage.setContent(message);
        chatMessageRepository.save(userMessage);

        List<String> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(chat -> chat.getRole() + ": " + chat.getContent())
                .toList();

        String reply = routeByIntent(intent, user, message, history);
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSession(session);
        assistantMessage.setRole("assistant");
        assistantMessage.setIntent(intent);
        assistantMessage.setContent(reply);
        chatMessageRepository.save(assistantMessage);

        List<ChatResponse.ChatMessageDto> responseHistory = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(chat -> new ChatResponse.ChatMessageDto(
                        chat.getRole(),
                        chat.getIntent() == null ? null : chat.getIntent().name(),
                        chat.getContent(),
                        chat.getCreatedAt()
                ))
                .toList();

        return new ChatResponse(session.getId(), session.getTitle(), intent.name(), reply, responseHistory, assistantMessage.getUpdatedAt());
    }

    private ChatSession createSession(String firstMessage) {
        ChatSession session = new ChatSession();
        session.setOwner(currentUserService.getCurrentUser());
        session.setTitle(firstMessage.length() > 30 ? firstMessage.substring(0, 30) + "..." : firstMessage);
        return chatSessionRepository.save(session);
    }

    private String routeByIntent(ChatIntent intent, UserAccount user, String message, List<String> history) {
        return switch (intent) {
            case RESUME -> buildResumeReply(user);
            case JOBS -> buildJobReply(user, message);
            case INTERVIEW -> buildInterviewReply(user, message);
            case GENERAL -> aiService.chat(message, history);
        };
    }

    private String buildResumeReply(UserAccount user) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findFirstByResumeOwnerIdOrderByCreatedAtDesc(user.getId())
                .orElse(null);
        if (analysis == null) {
            return "No resume analysis found yet. Upload a resume first, then I can provide ATS score, missing skills, and improvements.";
        }

        List<String> extracted = splitTokens(analysis.getExtractedSkills(), ",");
        List<String> missing = splitTokens(analysis.getMissingSkills(), ",");
        List<String> suggestions = splitTokens(analysis.getSuggestions(), "\\|\\|");

        return """
                Latest resume analysis:
                ATS Score: %d
                Extracted skills: %s
                Missing skills: %s
                Top suggestions:
                - %s
                """.formatted(
                analysis.getAtsScore(),
                String.join(", ", extracted.stream().limit(8).toList()),
                missing.isEmpty() ? "None" : String.join(", ", missing.stream().limit(5).toList()),
                suggestions.isEmpty() ? "Add quantified impact in recent projects." : String.join("\n- ", suggestions.stream().limit(4).toList())
        );
    }

    private String buildJobReply(UserAccount user, String message) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findFirstByResumeOwnerIdOrderByCreatedAtDesc(user.getId())
                .orElse(null);
        if (analysis == null) {
            return "Upload and analyze a resume first so I can rank jobs by your skills.";
        }

        String query = user.getPreferredRole();
        if (query == null || query.isBlank()) {
            query = user.getCurrentRole();
        }
        if (query == null || query.isBlank()) {
            query = "Software Engineer";
        }
        String location = (user.getPreferredLocation() == null || user.getPreferredLocation().isBlank())
                ? (user.getCity() == null || user.getCity().isBlank() ? "Remote" : user.getCity())
                : user.getPreferredLocation();

        String lower = message.toLowerCase(Locale.ENGLISH);
        if (lower.contains("remote")) {
            location = "Remote";
        }

        List<JobRecommendationResponse> jobs = jobRecommendationService.recommendJobs(analysis.getResume().getId(), query, location);
        if (jobs.isEmpty()) {
            return "No job recommendations found right now. Try again with a different role/location.";
        }

        StringBuilder builder = new StringBuilder("Top recommended jobs:\n");
        for (int i = 0; i < Math.min(5, jobs.size()); i++) {
            JobRecommendationResponse job = jobs.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(job.title())
                    .append(" | ")
                    .append(job.company())
                    .append(" | Match ")
                    .append(job.matchScore())
                    .append("% | ")
                    .append(job.applyLink())
                    .append("\n");
        }
        return builder.toString().trim();
    }

    private String buildInterviewReply(UserAccount user, String message) {
        String role = (user.getPreferredRole() == null || user.getPreferredRole().isBlank())
                ? (user.getCurrentRole() == null || user.getCurrentRole().isBlank() ? "Software Engineer" : user.getCurrentRole())
                : user.getPreferredRole();
        String focusArea = extractFocusArea(message);
        InterviewPreparationResponse prep = aiService.generateInterviewPreparation(role, focusArea);

        StringBuilder builder = new StringBuilder("Interview prep for ").append(prep.role()).append(":\n");
        prep.questionAnswers().stream().limit(3).forEach(qa -> builder
                .append("- Q: ").append(qa.question()).append("\n")
                .append("  A: ").append(qa.answer()).append("\n"));
        builder.append("Checklist:\n");
        prep.roadmap().stream().limit(4).forEach(item -> builder.append("- ").append(item).append("\n"));
        return builder.toString().trim();
    }

    private String extractFocusArea(String message) {
        String lower = message.toLowerCase(Locale.ENGLISH);
        if (lower.contains("system design")) {
            return "System Design";
        }
        if (lower.contains("aws")) {
            return "AWS";
        }
        if (lower.contains("spring")) {
            return "Spring Boot";
        }
        if (lower.contains("react")) {
            return "React";
        }
        return "Core role fundamentals";
    }

    private List<String> splitTokens(String value, String delimiterRegex) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(delimiterRegex))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }
}
