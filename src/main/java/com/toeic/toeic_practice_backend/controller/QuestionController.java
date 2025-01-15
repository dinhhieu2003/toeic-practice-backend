package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.question.AddTopicToQuestionRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.question.UpdateQuestionRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.question.UpdateQuestionResourceRequest;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.question.UpdateQuestionResourceResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.service.QuestionService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/questions")
@RequiredArgsConstructor
public class QuestionController {
	private final QuestionService questionService;
	
	@PostMapping("/topics")
	public ResponseEntity<Question> addTopicsToQuestion(@RequestBody AddTopicToQuestionRequest request) {
		List<String> listTopicIds = request.getListTopicIds();
		String questionId = request.getQuestionId();
		return ResponseEntity.ok(questionService.addTopicToQuestion(listTopicIds, questionId));
	}
	
	@PutMapping("/{questionId}/update/resource")
	public ResponseEntity<UpdateQuestionResourceResponse> updateResourceQuestion(@RequestBody UpdateQuestionResourceRequest res, @PathVariable String questionId) {
		return ResponseEntity.ok(questionService.updateResourceQuestion(res.getRes(), questionId));
	}
	
	@GetMapping("")
	public ResponseEntity<PaginationResponse<List<Question>>> getAllQuestionForPractice(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String partNum,
            @RequestParam(required = false) String topic,
			@RequestParam(required = false) String orderAscBy,
			@RequestParam(required = false) String orderDescBy) {
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		Map<String, String> filterParams = new HashedMap<>(); 
		filterParams.put("DIFFICULTY", difficulty);
		filterParams.put("PARTNUM", partNum);
		filterParams.put("TOPIC", topic);
		filterParams.put("ORDER_ASC_BY", orderAscBy);
    	filterParams.put("ORDER_DESC_BY", orderDescBy);
		return ResponseEntity.ok(questionService.getAllQuestionForPractice(pageable, filterParams));
	}
	
	@PutMapping("")
	public ResponseEntity<Question> updateQuestion(@RequestBody UpdateQuestionRequest updateQuestionRequest) {
		return ResponseEntity.ok(questionService.updateQuestion(updateQuestionRequest));
	}
}
