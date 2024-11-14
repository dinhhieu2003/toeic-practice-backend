package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.ResultMapper;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;
import com.toeic.toeic_practice_backend.repository.ResultRepository;
import com.toeic.toeic_practice_backend.repository.UserRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResultService {

	private final ResultRepository resultRepository;

	private final QuestionRepository questionRepository;
	
	private final UserRepository userRepository;

	private final ResultMapper resultMapper;

	public Result saveResult(Result result) {
		return resultRepository.save(result);
	}

	public PaginationResponse<List<TestResultResponse>> getAllResults(Pageable pageable, Map<String, String> filterParams) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
	        throw new AppException(ErrorCode.UNAUTHORIZED);
	    }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
		return PaginationResponse.<List<TestResultResponse>>builder()
			.meta(
				Meta.builder()
					.current(pageable.getPageNumber() + 1)
					.pageSize(pageable.getPageSize())
					.totalItems(resultPage.getTotalElements())
					.totalPages(resultPage.getTotalPages())
					.build()
			)
			.result(result)
			.build();
	}

	public TestResultResponse getById(String ResultId) {
		Result result = resultRepository.findById(ResultId).orElseThrow(() -> new AppException(ErrorCode.RESULT_NOT_FOUND));

		List<String> questionIds = result.getUserAnswers().stream()
            .map(Result.UserAnswer::getQuestionId)
            .distinct()
            .collect(Collectors.toList());

		List<Question> questions = questionRepository.findAllById(questionIds);

        Map<String, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, question -> question));

        result.getUserAnswers().forEach(userAnswer -> 
            userAnswer.setQuestion(questionMap.get(userAnswer.getQuestionId()))
        );

		return resultMapper.toTestResultResponse(
			result
		);
	}
}
