package com.toeic.toeic_practice_backend.domain.dto.response.question;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionResourceResponse {
	private String questionId;
	private List<Resource> resources = new ArrayList<>();
}
