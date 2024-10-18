package com.toeic.toeic_practice_backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.test.TestCreationRequest;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.TestRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestService {
	private final TestRepository testRepository;
	private final CategoryService categoryService;
	public Test addTest(TestCreationRequest testCreationRequest) {
		Optional<Test> testOptional = 
				testRepository.findByNameAndCategory_Id(testCreationRequest.getName(), 
						testCreationRequest.getCategoryId());
		Test testResponse = new Test();
		if(testOptional.isEmpty()) {
			String categoryId = testCreationRequest.getCategoryId();
			Category category = categoryService.findById(categoryId);
			Test newTest = new Test();
			newTest.setName(testCreationRequest.getName());
			newTest.setActive(true);
			newTest.setTotalQuestion(testCreationRequest.getTotalQuestion());
			newTest.setTotalScore(testCreationRequest.getTotalScore());
			newTest.setTotalUserAttempt(testCreationRequest.getTotalUserAttempt());
			newTest.setLimitTime(testCreationRequest.getLimitTime());
			newTest.setCategory(category);
			testResponse = testRepository.save(newTest);
		} else {
			throw new AppException(ErrorCode.TEST_ALREADY_EXISTS);
		}
		return testResponse;
	}
}
