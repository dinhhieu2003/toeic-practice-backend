package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.test.TestCreationRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.TestRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestService {
	private final TestRepository testRepository;
	public Test addTest(TestCreationRequest testCreationRequest) {
		Optional<Test> testOptional = 
				testRepository.findByNameAndCategoryId(testCreationRequest.getName(), 
						testCreationRequest.getCategoryId());
		Test testResponse = new Test();
		if(testOptional.isEmpty()) {
			String categoryId = testCreationRequest.getCategoryId();
			Test newTest = new Test();
			newTest.setName(testCreationRequest.getName());
			newTest.setActive(true);
			newTest.setTotalQuestion(testCreationRequest.getTotalQuestion());
			newTest.setTotalScore(testCreationRequest.getTotalScore());
			newTest.setTotalUserAttempt(testCreationRequest.getTotalUserAttempt());
			newTest.setLimitTime(testCreationRequest.getLimitTime());
			newTest.setCategoryId(categoryId);
			testResponse = testRepository.save(newTest);
		} else {
			throw new AppException(ErrorCode.TEST_ALREADY_EXISTS);
		}
		return testResponse;
	}
	
	public PaginationResponse<List<Test>> getAllTest(Pageable pageable) {
		Page<Test> testPage = testRepository.findAll(pageable);
		PaginationResponse<List<Test>> response = new PaginationResponse<List<Test>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(testPage.getTotalElements());
		meta.setTotalPages(testPage.getTotalPages());
		List<Test> result = testPage.getContent();
		response.setMeta(meta);
		response.setResult(result);
		return response;
	}
}
