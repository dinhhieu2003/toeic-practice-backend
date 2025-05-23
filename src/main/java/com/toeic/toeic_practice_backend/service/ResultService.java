package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultResponse;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.ResultMapper;
import com.toeic.toeic_practice_backend.repository.ResultRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResultService {

	private final ResultRepository resultRepository;

	private final UserService userService;
	
	private final ResultMapper resultMapper;

	public List<Result> getByTestIdAndUserId(String testId, String userId) {
		return resultRepository.findByTestIdAndUserId(testId, userId);
	}
	
	public List<Result> getByUserIdAndListTestId(String userId, List<String> listTestId) {
		return resultRepository.findByUserIdAndTestIdIn(userId, listTestId);
	}
	
	public Result saveResult(Result result) {
		return resultRepository.save(result);
	}
	
	public void saveAllResult(List<Result> results) {
		resultRepository.saveAll(results);
	}

	public List<Result> getByTestId(String testId) {
		List<Result> results = resultRepository.findByTestId(testId);
		return results;
	}
	
	public PaginationResponse<List<TestResultResponse>> getAllResults(Pageable pageable, Map<String, String> filterParams) {
        String email = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User user = userService.getUserByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		Page<Result> resultPage;

		String practiceFilter = filterParams.get("TYPE");

		if (practiceFilter != null) {
			switch (practiceFilter) {
				case "FULL_TEST":
					resultPage  = resultRepository.findWithoutUserAnswersByUserIdAndTypeFullTest(user.getId(), pageable);
					break;
				case "PRACTICE":
					resultPage  = resultRepository.findWithoutUserAnswersByUserIdAndTypePractice(user.getId(), pageable);
					break;
				case "QUESTION":
					resultPage  = resultRepository.findByUserIdAndTestIdEmpty(user.getId(), pageable);
					break;
				default:
					resultPage  = resultRepository.findWithoutUserAnswersByUserId(user.getId(), pageable);
					break;
			}
		} else {
			// Nếu không có giá trị "PRACTICE" trong filterParams, thực hiện một hành động mặc định
			resultPage = resultRepository.findWithoutUserAnswersByUserId(user.getId(), pageable);
		}

		if(resultPage.isEmpty()) throw new AppException(ErrorCode.RESULT_NOT_FOUND);

		List<TestResultResponse> result = resultMapper.toTestResultResponseList(resultPage.getContent());

		// Tạo PaginationResponse
		Meta meta = Meta.builder()
				.current(pageable.getPageNumber() + 1)
				.pageSize(pageable.getPageSize())
				.totalItems(resultPage.getTotalElements())
				.totalPages(resultPage.getTotalPages())
				.build();
		return PaginationUtils.buildPaginationResponse(meta, result);
	}
	
	public Result getByIdMobile(String resultId) {
		Result result = resultRepository.findById(resultId).orElseThrow(() -> new AppException(ErrorCode.RESULT_NOT_FOUND));
		List<UserAnswer> userAnswers = result.getUserAnswers();
		userAnswers = userAnswers.stream()
				.filter(userAnswer -> !"subquestion".equals(userAnswer.getType()))
			    .collect(Collectors.toList());
		result.setUserAnswers(userAnswers);
		return result;
	}
	
	public List<UserAnswer> getReview(String resultId) {
		Result result = resultRepository.findById(resultId).orElseThrow(() -> new AppException(ErrorCode.RESULT_NOT_FOUND));
		List<UserAnswer> userAnswers = result.getUserAnswers();
		List<UserAnswer> finalUserAnswers = userAnswers.stream()
			    .filter(userAnswer -> !"subquestion".equals(userAnswer.getType()))
			    .collect(Collectors.toList());
		return finalUserAnswers;
	}
	
	public List<Result> getResultsByUserIdWithoutUserAnswer(String userId) {
		return resultRepository.findWithoutUserAnswersByUserId(userId);
	}
}
