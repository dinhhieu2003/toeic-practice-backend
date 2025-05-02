package com.toeic.toeic_practice_backend.domain.dto.internal;

import java.util.List;
import java.util.Map;

/**
 * A comprehensive DTO containing all user information needed by the recommender system.
 */
public record UserProfileInternalDTO(
    String userId,
    int target,
    int averageListeningScore,
    int averageReadingScore,
    int averageTotalScore,
    int highestScore,
    List<TopicStatInternalDTO> topicStats,
    List<SkillStatInternalDTO> skillStats,
    Map<String, Integer> learningProgress, // {lectureId: percent}
    List<TestAttemptStatDTO> testHistory
) {} 