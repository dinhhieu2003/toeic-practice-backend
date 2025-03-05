package com.toeic.toeic_practice_backend.domain.dto.request.chatgpt;

import java.util.List;

public record ChatGPTRequest(String model, List<Message> messages) {
    public static record Message(String role, String content) {}
}