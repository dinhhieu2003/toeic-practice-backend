package com.toeic.toeic_practice_backend.domain.dto.response.test;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullTestResponse {
	private List<Part> parts = new ArrayList<>();
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Part {
		private int partNum;
		private List<MultipleChoiceQuestion> questionList = new ArrayList<>();
	}
			
}
