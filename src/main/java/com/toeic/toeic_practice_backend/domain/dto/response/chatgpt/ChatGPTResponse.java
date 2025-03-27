package com.toeic.toeic_practice_backend.domain.dto.response.chatgpt;

import java.util.List;

public record ChatGPTResponse(List<Choice> choices) {
    public static record Choice(Message message) {
        public static record Message(String role, String content) {}
    }
}
