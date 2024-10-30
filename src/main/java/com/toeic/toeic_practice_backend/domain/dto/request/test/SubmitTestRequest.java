package com.toeic.toeic_practice_backend.domain.dto.request.test;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTestRequest {
	private List<AnswerPair> userAnswer = new ArrayList<>();
	private int totalSeconds;
	private String testId;
	private String parts;
	private String type;	// fulltest or practice
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AnswerPair {
		private String questionId;
		private String userAnswer;
		private int timeSpent;
	}
}
