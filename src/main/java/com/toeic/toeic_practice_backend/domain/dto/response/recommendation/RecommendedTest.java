package com.toeic.toeic_practice_backend.domain.dto.response.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedTest {
	private String id;	// recommend id - its value is test label: testId or "Lam lai"
	private String name;
	private float score;
	private String explanation;
	private String testId;
}
