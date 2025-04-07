package com.toeic.toeic_practice_backend.domain.dto.response.chatgpt;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorResponse {
    private String sessionId;
    private ChatGPTResponse chatResponse;
} 