package com.careerassistant.service;

import com.careerassistant.entity.ChatIntent;

public interface IntentDetectionService {
    ChatIntent detectIntent(String message);
}
