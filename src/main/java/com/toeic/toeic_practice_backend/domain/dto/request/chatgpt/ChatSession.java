package com.toeic.toeic_practice_backend.domain.dto.request.chatgpt;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    private String sessionId;
    private String questionId;
    private String questionText;
    private String correctAnswer;
    
    @Builder.Default
    private List<ChatGPTRequest.Message> messages = new ArrayList<>();
} 