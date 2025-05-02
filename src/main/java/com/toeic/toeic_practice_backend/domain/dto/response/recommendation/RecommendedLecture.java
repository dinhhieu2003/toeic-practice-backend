package com.toeic.toeic_practice_backend.domain.dto.response.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedLecture {
	private String id;	// its value is lecture label - lectureId or "Hoc tiep"
	private float score;
	private String explanation;
	private String lectureId;
}
