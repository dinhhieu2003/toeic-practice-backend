package com.toeic.toeic_practice_backend.domain.dto.internal;

import java.util.List;
import java.util.Map;

/**
 * A streamlined DTO containing only the user information required for calculating user similarities
 * in the recommender system.
 */
public record UserSimilarityProfileDTO(
    String userId,
    int target,
    int averageListeningScore,
    int averageReadingScore,
    int averageTotalScore,
    List<TestAttemptStatDTO> testHistory, // Needed for collaborative score
    Map<String, Integer> learningProgress // Needed for collaborative score
) {} 