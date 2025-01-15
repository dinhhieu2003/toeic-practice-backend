package com.toeic.toeic_practice_backend.domain.dto.request.question;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionResourceRequest {
	private List<Resource> res = new ArrayList<>();
}
