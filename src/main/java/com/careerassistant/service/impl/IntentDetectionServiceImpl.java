package com.careerassistant.service.impl;

import com.careerassistant.entity.ChatIntent;
import com.careerassistant.service.IntentDetectionService;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class IntentDetectionServiceImpl implements IntentDetectionService {

    @Override
    public ChatIntent detectIntent(String message) {
        String lower = message.toLowerCase(Locale.ENGLISH);
        if (containsAny(lower, "resume", "cv", "ats", "keyword", "improve resume")) {
            return ChatIntent.RESUME;
        }
        if (containsAny(lower, "job", "apply", "opening", "vacancy", "company")) {
            return ChatIntent.JOBS;
        }
        if (containsAny(lower, "interview", "question", "mock", "prep")) {
            return ChatIntent.INTERVIEW;
        }
        return ChatIntent.GENERAL;
    }

    private boolean containsAny(String input, String... tokens) {
        for (String token : tokens) {
            if (input.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
