package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.question.AddTopicToQuestionRequest;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.service.QuestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {
	private final QuestionService questionService;
	
	@PostMapping("/topics")
	public ResponseEntity<Question> addTopicsToQuestion(@RequestBody AddTopicToQuestionRequest request) {
		List<String> listTopicIds = request.getListTopicIds();
		String questionId = request.getQuestionId();
		return ResponseEntity.ok(questionService.addTopicToQuestion(listTopicIds, questionId));
	}
	
}
