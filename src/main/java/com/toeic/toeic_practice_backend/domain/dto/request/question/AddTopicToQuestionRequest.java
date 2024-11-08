package com.toeic.toeic_practice_backend.domain.dto.request.question;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTopicToQuestionRequest {
	private List<String> listTopicIds = new ArrayList<>();
	private String questionId;
}
