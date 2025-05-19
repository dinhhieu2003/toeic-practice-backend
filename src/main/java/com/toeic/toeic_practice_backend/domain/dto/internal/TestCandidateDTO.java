package com.toeic.toeic_practice_backend.domain.dto.internal;

import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Topic;

/**
 * A DTO representing a test that could be recommended to a user.
 */
public record TestCandidateDTO(
    String testId,
    int difficulty,
    List<String> topics,	// topic names
    int totalUserAttempt
) {} 