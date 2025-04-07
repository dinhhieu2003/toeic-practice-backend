package com.toeic.toeic_practice_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.PromptRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.chatgpt.ChatGPTResponse;
import com.toeic.toeic_practice_backend.service.ChatGptService;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatMessageRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.chatgpt.TutorResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/chatgpt")
@RequiredArgsConstructor
public class ChatGptController {
    private final ChatGptService chatGptService;
    /**
     * Endpoint for the TOEIC question tutoring feature
     * If sessionId is not provided, a new session will be created
     */
    @PostMapping("/tutor")
    public ResponseEntity<TutorResponse> getTutorResponse(@RequestBody ChatMessageRequest request) {
        String sessionId = request.getSessionId(); // Can be null for first message
        String questionId = request.getQuestionId();
        String message = request.getMessage();
        
        // Process the user's question about this specific TOEIC question
        TutorResponse response = chatGptService.processTutorQuestion(sessionId, questionId, message);
        
        return ResponseEntity.ok(response);
    }
}
