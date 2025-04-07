package com.toeic.toeic_practice_backend.domain.dto.request.chatgpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    private String sessionId;
    private String questionId;
    private String message;
} 