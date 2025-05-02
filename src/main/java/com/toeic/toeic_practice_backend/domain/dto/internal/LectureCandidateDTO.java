package com.toeic.toeic_practice_backend.domain.dto.internal;

import java.time.Instant;
import java.util.List;

/**
 * A DTO representing a lecture that could be recommended to a user.
 */
public record LectureCandidateDTO(
    String lectureId,
    List<String> topicIds,
    Instant createdAt
) {} 