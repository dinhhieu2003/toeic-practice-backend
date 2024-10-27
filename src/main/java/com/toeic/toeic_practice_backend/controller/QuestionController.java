package com.toeic.toeic_practice_backend.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.service.QuestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {
	private final QuestionService questionService;
	
	@GetMapping()
	public ResponseEntity<PaginationResponse<List<Question>>> getAllQuestion(
			@RequestParam(defaultValue = "1") String current,
			@RequestParam(defaultValue = "5") String pageSize,
			@RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String partNum,
            @RequestParam(required = false) String topic,
			@RequestParam(required = false) String orderAscBy,
			@RequestParam(required = false) String orderDescBy) {
		int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
		Map<String, String> filterParams = new HashedMap<>(); 
		filterParams.put("DIFFICULTY", difficulty);
		filterParams.put("PARTNUM", partNum);
		filterParams.put("TOPIC", topic);
		filterParams.put("ORDER_ASC_BY", orderAscBy);
    	filterParams.put("ORDER_DESC_BY", orderDescBy);
		return ResponseEntity.ok(questionService.getAllQuestion(pageable, filterParams));
	}
}
