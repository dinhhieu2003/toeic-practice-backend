package com.toeic.toeic_practice_backend.domain.dto.internal;

/**
 * A DTO representing a test attempt statistic, mirroring the User.TestAttemptStat entity.
 */
public record TestAttemptStatDTO(
    String testId,
    int avgScore,
    int attempt
) {} 