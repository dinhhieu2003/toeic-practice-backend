package com.toeic.toeic_practice_backend.domain.dto.response.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullTestResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int totalQuestion;
	private List<MultipleChoiceQuestion> listMultipleChoiceQuestions = new ArrayList<>();
			
}
