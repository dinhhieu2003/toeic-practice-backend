package com.toeic.toeic_practice_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.testDraft.TestDraftRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.testDraft.CheckTestDraftExistResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.testDraft.TestDraftResponse;
import com.toeic.toeic_practice_backend.service.TestDraftService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/testDrafts")
@RequiredArgsConstructor
public class TestDraftController {
	private final TestDraftService testDraftService;
	
	@GetMapping("/check-exist/{testId}")
	public ResponseEntity<CheckTestDraftExistResponse> checkExist(
			@PathVariable String testId) {
		return ResponseEntity.ok(testDraftService.checkExist(testId));
	}
	
	@GetMapping("/{testId}")
	public ResponseEntity<TestDraftResponse> getTestDraft(
			@PathVariable String testId) {
		return ResponseEntity.ok(testDraftService.getTestDraft(testId));
	}
	
	@PutMapping("")
	public ResponseEntity<?> syncTestDraft(
			@RequestBody TestDraftRequest request) {
		testDraftService.syncTestDraft(request);
		return ResponseEntity.ok(null);
	}
	
	@DeleteMapping("/{testId}")
	public ResponseEntity<?> deleteTestDraft(
			@PathVariable String testId) {
		testDraftService.deleteTestDraft(testId);
		return ResponseEntity.ok(null);
	}
	
}
