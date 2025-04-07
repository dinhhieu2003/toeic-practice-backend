package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.ContentPart;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.Message;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.TextPart;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.PromptRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.chatgpt.ChatGPTResponse;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatSession;
import com.toeic.toeic_practice_backend.domain.dto.response.chatgpt.TutorResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGptService {
    private static final Logger log = LoggerFactory.getLogger(ChatGptService.class);

    // ChatGpt params
    @Value("${chatgpt.api.url}")
    private String chatGptApiUrl;
    @Value("${chatgpt.api.model}")
    private String chatGptModel;
    @Value("${chatgpt.api.key}")
    private String chatGptApiKey;

    private final RestTemplate restTemplate;
    private final QuestionService questionService;
    private final ChatSessionService chatSessionService;

    /**
     * Process a user message for a specific TOEIC question
     * @param sessionId Optional - if null, a new session will be created
     * @param questionId Required
     * @param userMessage Required
     * @return TutorResponse with sessionId and ChatGPT response
     */
    public TutorResponse processTutorQuestion(String sessionId, String questionId, String userMessage) {
        // Get question details
        Question question = questionService.getQuestionById(questionId);
        
        // Get transcript, handling subquestions specially
        String transcript = question.getTranscript();
        if ("subquestion".equals(question.getType()) && (transcript == null || transcript.isEmpty()) && question.getParentId() != null) {
            // For subquestions, try to get transcript from parent
            try {
                Question parentQuestion = questionService.getQuestionById(question.getParentId());
                if (parentQuestion != null && parentQuestion.getTranscript() != null && !parentQuestion.getTranscript().isEmpty()) {
                    transcript = parentQuestion.getTranscript();
                }
            } catch (Exception e) {
                // Log but continue if parent not found
                log.warn("Could not retrieve parent question for transcript: {}", e.getMessage());
            }
        }
        // Get or create chat session
        ChatSession chatSession = chatSessionService.getOrCreateSession(
            sessionId,
            questionId,
            question,
            transcript
        );
        // Add user message to session
        if(userMessage == null || userMessage.isEmpty()) {
        	Message firstUserPromptMessage = chatSessionService.buildFirstUserPromptMessage(question);
        	chatSession.getMessages().add(firstUserPromptMessage);       	
        } else {
            chatSessionService.addMessage(chatSession, "user", new ArrayList<>(Collections.singletonList(new TextPart(userMessage))));
        }
        // Create chat request with full conversation history
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(
            chatGptModel,
            chatSession.getMessages()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + chatGptApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request entity with body and header
        HttpEntity<ChatGPTRequest> requestEntity = new HttpEntity<>(chatGPTRequest, headers);

        ChatGPTResponse chatGPTResponse = restTemplate.postForObject(
            chatGptApiUrl,
            requestEntity,
            ChatGPTResponse.class
        );
        
        // Store assistant's response in the session
        if (chatGPTResponse != null && !chatGPTResponse.choices().isEmpty()) {
            String assistantResponse = chatGPTResponse.choices().get(0).message().content();
            chatSessionService.addMessage(chatSession, "assistant", new ArrayList<>(Collections.singletonList(new TextPart(assistantResponse))));
        }

        // Build response with sessionId
        return TutorResponse.builder()
                .sessionId(chatSession.getSessionId())
                .chatResponse(chatGPTResponse)
                .build();
    }
}
