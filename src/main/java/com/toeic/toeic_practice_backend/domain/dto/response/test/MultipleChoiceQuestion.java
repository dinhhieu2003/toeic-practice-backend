package com.toeic.toeic_practice_backend.domain.dto.response.test;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleChoiceQuestion {
	private String id;
	private int questionNum;
	private int partNum;
	private String type;
	private List<MultipleChoiceQuestion> subQuestions = new ArrayList<>();
	private String content;
	private List<Question.Resource> resources = new ArrayList<>();
	private List<String> answers = new ArrayList<>();
}