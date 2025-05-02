package com.toeic.toeic_practice_backend.domain.dto.internal;

/**
 * A DTO representing topic-specific statistics for a user, optimized for internal API usage.
 */
public record TopicStatInternalDTO(
    String topicName,
    int totalCorrect,
    int totalIncorrect
) {} 