package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	
}
