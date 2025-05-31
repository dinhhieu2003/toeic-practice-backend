package com.toeic.toeic_practice_backend.service;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.testDraft.TestDraftRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.testDraft.CheckTestDraftExistResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.testDraft.TestDraftResponse;
import com.toeic.toeic_practice_backend.domain.entity.TestDraft;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.TestDraftRepository;
import com.toeic.toeic_practice_backend.repository.projectionInterface.TestDraftVersionOnly;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestDraftService {
	private final TestDraftRepository testDraftRepository;
	private final UserService userService;
	
	public CheckTestDraftExistResponse checkExist(String testId) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByEmail(username);
        if(userOptional.isEmpty()) {
        	return new CheckTestDraftExistResponse(false, null);
        }
        String userId = userOptional.get().getId();
        boolean exist = testDraftRepository.existsByTestIdAndUserId(testId, userId);
        Float version = null;
        if(exist) {
        	TestDraftVersionOnly testDraftVersion = testDraftRepository.findVersionByTestIdAndUserId(testId, userId)
        			.orElseThrow(() -> new AppException(ErrorCode.TEST_DRAFT_NOT_FOUND));
        	version = testDraftVersion.getVersion();
        }
        return new CheckTestDraftExistResponse(exist, version);
	}
	
	public TestDraftResponse getTestDraft(String testId) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByEmail(username)
        		.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        String userId = user.getId();
        TestDraft testDraft = testDraftRepository.findByTestIdAndUserId(testId, userId)
        		.orElseThrow(() -> new AppException(ErrorCode.TEST_DRAFT_NOT_FOUND));
		return new TestDraftResponse(testId, testDraft.getDraftData(), testDraft.getVersion());
	}
	
	public void syncTestDraft(TestDraftRequest request) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByEmail(username)
        		.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        String userId = user.getId();
        Optional<TestDraft> testDraftOptional = testDraftRepository.findByTestIdAndUserId(request.getTestId(), userId);
        if(testDraftOptional.isEmpty()) {
        	TestDraft newTestDraft = new TestDraft();
        	newTestDraft.setTestId(request.getTestId());
        	newTestDraft.setUserId(userId);
        	newTestDraft.setActive(true);
        	newTestDraft.setDraftData(request.getDraftData());
        	newTestDraft.setVersion(request.getVersion());
        	testDraftRepository.save(newTestDraft);
        	return;
        }
        TestDraft existTestDraft = testDraftOptional.get();
        existTestDraft.setDraftData(request.getDraftData());
        existTestDraft.setVersion(request.getVersion());
        testDraftRepository.save(existTestDraft);
        return;
	}
	
	public void deleteTestDraft(String testId) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByEmail(username)
        		.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        String userId = user.getId();
        TestDraft testDraft = testDraftRepository.findByTestIdAndUserId(testId, userId)
        		.orElseThrow(() -> new AppException(ErrorCode.TEST_DRAFT_NOT_FOUND));
        testDraftRepository.delete(testDraft);
	}
}
