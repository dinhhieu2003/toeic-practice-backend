package com.toeic.toeic_practice_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.PromptRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.chatgpt.ChatGPTResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatGptService {
    // ChatGpt params
    @Value("${chatgpt.api.url}")
    private String chatGptApiUrl;
    @Value("${chatgpt.api.model}")
    private String chatGptModel;
    @Value("${chatgpt.api.key}")
    private String chatGptApiKey;

    private final RestTemplate restTemplate;

    /**
     * Get response from ChatGPT API
     * @param promptRequest
     * @return chatGPTResponse
     */
    public ChatGPTResponse getChatGptResponse(PromptRequest promptRequest) {
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(
            chatGptModel,
            List.of(new ChatGPTRequest.Message("user", promptRequest.prompt()))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + chatGptApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tạo request entity với body và header
        HttpEntity<ChatGPTRequest> requestEntity = new HttpEntity<>(chatGPTRequest, headers);

        ChatGPTResponse chatGPTResponse = restTemplate.postForObject(
            chatGptApiUrl,
            requestEntity,
            ChatGPTResponse.class
        );

        return chatGPTResponse;
    }
}
