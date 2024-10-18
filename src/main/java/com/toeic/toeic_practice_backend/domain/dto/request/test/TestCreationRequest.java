package com.toeic.toeic_practice_backend.domain.dto.request.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCreationRequest {
	private String name;
	private String categoryId;
    private int totalUserAttempt;
    private int totalQuestion;
    private int totalScore;
    private int limitTime;
}