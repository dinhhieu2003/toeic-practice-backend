package com.toeic.toeic_practice_backend.controller;

import com.toeic.toeic_practice_backend.domain.dto.response.recommendation.RecommendationResponse;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.service.RecommendationIntegrationService;
import com.toeic.toeic_practice_backend.service.UserService;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
@Slf4j
public class RecommendationController {
	
    private final RecommendationIntegrationService recommendationIntegrationService;
    private final UserService userService;

    /**
     * Get personalized recommendations for the currently authenticated user
     * 
     * @return ResponseEntity containing recommended tests and lectures
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyRecommendations() {
        // Get the current authenticated user
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(username.equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required to get recommendations");
        }
        
        User user = userService.getUserByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String userId = user.getId();
        log.info("Fetching recommendations for authenticated user: {}", userId);

        try {
            // Get recommendations from the recommender service
            RecommendationResponse recommendationsResponse = 
                    recommendationIntegrationService.getRecommendations(userId);

            if (recommendationsResponse == null) {
                log.warn("No recommendations received for user: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Could not retrieve recommendations at this time");
            }

            
            return ResponseEntity
            		.ok()
//            		.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic())
            		.body(recommendationsResponse);

        } catch (Exception e) {
            log.error("Error processing recommendations for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing recommendations");
        }
    }
} 