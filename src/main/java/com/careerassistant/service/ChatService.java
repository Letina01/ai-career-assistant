package com.careerassistant.service;

import com.careerassistant.dto.chat.ChatResponse;

public interface ChatService {
    ChatResponse chat(Long sessionId, String message);
}
