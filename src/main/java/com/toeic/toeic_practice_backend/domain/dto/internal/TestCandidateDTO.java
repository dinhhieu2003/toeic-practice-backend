package com.toeic.toeic_practice_backend.domain.dto.internal;

import java.util.List;

/**
 * A DTO representing a test that could be recommended to a user.
 */
public record TestCandidateDTO(
    String testId,
    int difficulty,
    List<String> topicIds,
    int totalUserAttempt
) {} 