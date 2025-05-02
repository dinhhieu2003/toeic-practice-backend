package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.internal.LectureCandidateDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.TestCandidateDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.UserProfileInternalDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.UserSimilarityProfileDTO;
import com.toeic.toeic_practice_backend.service.InternalApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for internal API endpoints used by the recommender system.
 * All endpoints are protected by the API key authentication filter.
 */
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal API", description = "Protected endpoints for internal microservices")
public class InternalApiController {

    private final InternalApiService internalApiService;

    @GetMapping("/users/{userId}/profile")
    @Operation(summary = "Get user profile for recommendation", 
               description = "Retrieves a comprehensive user profile with all statistics needed by the recommender system")
    public ResponseEntity<UserProfileInternalDTO> getUserProfile(@PathVariable String userId) {
        log.info("Internal API request: Get user profile, userId: {}", userId);
        UserProfileInternalDTO userProfile = internalApiService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/users/profiles-for-similarity")
    @Operation(summary = "Get all user profiles for similarity calculation", 
               description = "Retrieves a streamlined version of all user profiles optimized for user similarity calculations")
    public ResponseEntity<List<UserSimilarityProfileDTO>> getAllUserProfilesForSimilarity() {
        log.info("Internal API request: Get all user profiles for similarity");
        List<UserSimilarityProfileDTO> userProfiles = internalApiService.getAllUserProfilesForSimilarity();
        return ResponseEntity.ok(userProfiles);
    }

    @GetMapping("/tests/candidates")
    @Operation(summary = "Get test candidates", 
               description = "Retrieves all active tests as candidates for recommendation")
    public ResponseEntity<List<TestCandidateDTO>> getTestCandidates() {
        log.info("Internal API request: Get test candidates");
        List<TestCandidateDTO> testCandidates = internalApiService.getTestCandidates();
        return ResponseEntity.ok(testCandidates);
    }

    @GetMapping("/lectures/candidates")
    @Operation(summary = "Get lecture candidates", 
               description = "Retrieves all active lectures as candidates for recommendation")
    public ResponseEntity<List<LectureCandidateDTO>> getLectureCandidates() {
        log.info("Internal API request: Get lecture candidates");
        List<LectureCandidateDTO> lectureCandidates = internalApiService.getLectureCandidates();
        return ResponseEntity.ok(lectureCandidates);
    }
} 