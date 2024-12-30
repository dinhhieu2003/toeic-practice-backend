package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.topic.UpdateTopicStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.topic.UpdateTopicStatusResponse;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.service.TopicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {
	private final TopicService topicService;
	
	@PostMapping("")
	public ResponseEntity<Topic> addTopic(@RequestBody Topic topic) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(topicService.addTopic(topic));
	}
	
	@GetMapping("")
	public ResponseEntity<List<Topic>> getAllTopics() {
		return ResponseEntity.ok(topicService.getAllTopics());
	}
	
	@GetMapping("/pagination")
	public ResponseEntity<PaginationResponse<List<Topic>>> getTopicPage(
			@RequestParam(defaultValue = "1") String current,
			@RequestParam(defaultValue = "5") String pageSize,
			@RequestParam(required = false, defaultValue = "") String search) {
		int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
		return ResponseEntity.ok(topicService.getTopicPage(search, pageable));
	}
	
	@PutMapping("{topicId}/status")
	public ResponseEntity<UpdateTopicStatusResponse> updateTopicStatus(
			@PathVariable String topicId,
			@RequestBody UpdateTopicStatusRequest updateTopicStatusRequest) {
		return ResponseEntity.ok(topicService.updateTopicStatus(topicId, updateTopicStatusRequest));
	}
	
}
