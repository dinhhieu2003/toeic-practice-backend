package com.toeic.toeic_practice_backend.domain.dto.internal;

/**
 * A DTO representing skill-specific statistics for a user, optimized for internal API usage.
 * The skill can be 'listening' or 'reading'.
 */
public record SkillStatInternalDTO(
    String skill,
    int totalCorrect,
    int totalIncorrect
) {} 