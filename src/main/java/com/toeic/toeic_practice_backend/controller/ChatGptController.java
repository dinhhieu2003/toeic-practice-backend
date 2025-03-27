package com.toeic.toeic_practice_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.PromptRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.chatgpt.ChatGPTResponse;
import com.toeic.toeic_practice_backend.service.ChatGptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/chatgpt")
@RequiredArgsConstructor
public class ChatGptController {
    private final ChatGptService chatGptService;

    @PostMapping
    public ResponseEntity<ChatGPTResponse> getChatGptResponse(@RequestBody PromptRequest promptRequest) {
        return ResponseEntity.ok(chatGptService.getChatGptResponse(promptRequest));
    }
}
