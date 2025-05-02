package com.toeic.toeic_practice_backend.domain.dto.response.recommendation;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
	private String userId;
	private List<RecommendedTest> recommendedTests = new ArrayList<>();
	private List<RecommendedLecture> recommendedLectures = new ArrayList<>();
}
