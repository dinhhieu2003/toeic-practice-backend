package com.toeic.toeic_practice_backend.domain.dto.internal;

import java.time.Instant;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Topic;

/**
 * A DTO representing a lecture that could be recommended to a user.
 */
public record LectureCandidateDTO(
    String lectureId,
    String name,
    List<String> topics,	// topic names
    Instant createdAt
) {} 