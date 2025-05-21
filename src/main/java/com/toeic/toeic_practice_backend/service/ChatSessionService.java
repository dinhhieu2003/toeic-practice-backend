package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatSession;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.ContentPart;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.ImageUrl;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.ImageUrlPart;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.Message;
import com.toeic.toeic_practice_backend.domain.dto.request.chatgpt.ChatGPTRequest.TextPart;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Session TTL in hours
    private static final long SESSION_TTL = 24;
    
    /**
     * Get or create a chat session
     * If sessionId is null, a new one will be generated
     */
    public ChatSession getOrCreateSession(String sessionId, String questionId, Question question, String transcript) {
        // Generate a new sessionId if none is provided
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = generateSessionId();
        }
        
        String key = generateSessionKey(sessionId, questionId);
        String sessionData = redisTemplate.opsForValue().get(key);
        
        if (sessionData != null) {
            try {
                return objectMapper.readValue(sessionData, ChatSession.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing chat session: {}", e.getMessage());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
        
        // System prompt message
        Message systemPromptMessage = buildSystemPromptMessage(questionId, question, transcript);
        List<Message> messages = new ArrayList<>();
        messages.add(systemPromptMessage);
        
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .questionId(questionId)
                .questionText(question.getContent())
                .correctAnswer(question.getCorrectAnswer())
                .messages(messages)
                .build();
        
        saveSession(chatSession);
        return chatSession;
    }
    
    public Message buildFirstUserPromptMessage(Question question) {
    	List<ContentPart> content = new ArrayList<>();
    	StringBuilder userPromptBuilder = new StringBuilder();
    	userPromptBuilder.append("Hi! I'm about to ask you a TOEIC-related question. "
    			+ "Please take a moment to review the image context below (if available), "
    			+ "if image is unavailable, respond with 'Ready' and 'Ready, i can read image' (if you can read image) when you're prepared to begin."
    			+ "Please only response 'Ready' or 'Ready, i can read image'");
    	TextPart userPromptTextPart = new TextPart(userPromptBuilder.toString());
    	content.add(userPromptTextPart);
    	// Add image
    	List<Resource> resources = question.getResources();
    	for(Resource resource: resources) {
    		if ("image".equalsIgnoreCase(resource.getType())) {
    			// Image part of system prompt
    			String url = resource.getContent();
    			ImageUrlPart systemPromptImageUrlPart = 
    					new ImageUrlPart(new ImageUrl(url, "high"));
    			content.add(systemPromptImageUrlPart);
    		}
        }
        Message message = new Message("user", content);
        return message;
    }
    
    private Message buildSystemPromptMessage(String questionId, Question question, String transcript) {
    	List<String> answers = question.getAnswers();
        String formatedAnswers = String.join(" # ", answers);
        // Create system prompt message with transcript, explanation, and correct answer
        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append("You are a TOEIC expert and serve as a teaching assistant for a TOEIC practice website. \r\n"
        		+ "Your role is to help learners understand the reasoning behind the correct answer to a specific TOEIC question. \r\n"
        		+ "Provide clear, concise, and educational explanations, as a qualified TOEIC instructor would.")
            .append("You are embedded under TOEIC question #").append(questionId).append(".\n\n")
            .append("QUESTION: ").append(question.getContent()).append("\n")
            .append("ANSWER CHOICES: The following are the answer choices for this question. "
            		+ "Each choice is separated by the symbol '#' and is presented in order (A, B, C, D...). "
            		+ "Please refer to them by their order number when needed.\n")
            .append("ANSWERS LIST: ").append(formatedAnswers)
            .append("PART NUMBER (Please answer according to the specific characteristics of this part): ").append(question.getPartNum()).append("\n");
        
        // Add transcript if available
        if (transcript != null && !transcript.isEmpty()) {
            systemPromptBuilder.append("TRANSCRIPT: ").append(transcript).append("\n")
                .append("Note: The transcript is the full text of the audio (in listening sections) or the reading passage (in reading sections) related to this question.\n\n");
        }
        
        // Add explanation if available
        if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
            systemPromptBuilder.append("EXPLANATION: ").append(question.getExplanation()).append("\n\n");
        }
        
        systemPromptBuilder.append("CORRECT ANSWER: ").append(question.getCorrectAnswer()).append("\n\n")
            .append("You must ONLY respond to questions related to this specific TOEIC question. ")
            .append("If a user asks about anything unrelated to this question, politely refuse ")
            .append("and remind them you can only discuss this particular TOEIC question.")
            .append("Please respond in Vietnamese using a friendly and educational tone. ")
            .append("Use 'thầy' to refer to yourself and 'em' to refer to the learner. ")
            .append("Your style should be warm, supportive, and easy to understand for TOEIC learners.\n");
        TextPart systemPromptTextPart = new TextPart(systemPromptBuilder.toString());
        Message message = new Message("system", new ArrayList<>(Collections.singletonList(systemPromptTextPart)));
        return message;
    }
    
    /**
     * Generate a unique session ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Add a message to the chat session
     */
    public void addMessage(ChatSession chatSession, String role, List<ContentPart> content) {
        chatSession.getMessages().add(new Message(role, content));
        saveSession(chatSession);
    }
    
    /**
     * Save chat session to Redis
     */
    private void saveSession(ChatSession chatSession) {
        try {
            String key = generateSessionKey(chatSession.getSessionId(), chatSession.getQuestionId());
            String sessionData = objectMapper.writeValueAsString(chatSession);
            System.out.println(sessionData);
            redisTemplate.opsForValue().set(key, sessionData);
            redisTemplate.expire(key, SESSION_TTL, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Error serializing chat session: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    /**
     * Generate Redis key for session
     */
    private String generateSessionKey(String sessionId, String questionId) {
        return "chat_session:" + sessionId + ":" + questionId;
    }
} 