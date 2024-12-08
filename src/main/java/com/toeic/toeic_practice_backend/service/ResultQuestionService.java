package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.result.ResultSummaryResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.result.ResultSummaryResponse.UserAnswerResult;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.ResultRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResultQuestionService {
	private final QuestionService questionService;
	private final ResultRepository resultRepository;
	private final TestService testService;
	
	public ResultSummaryResponse getResultSummaryById(String resultId) {
	    Result result = resultRepository.findById(resultId)
	        .orElseThrow(() -> new AppException(ErrorCode.RESULT_NOT_FOUND));
	    
	    // get test name
	    String testName = ""; 
	    
	    if(result.getTestId() != null && !result.getTestId().isEmpty() && !result.getTestId().isBlank()) {
	    	testName = testService.getTestById(result.getTestId()).getName();
	    }
	    
	    
	    
	    // get userAnswer
	    List<UserAnswer> userAnswers = result.getUserAnswers();

	    // Store map userAnswer to get parent of subquestion
	    Map<String, UserAnswer> userAnswerMap = userAnswers
	        .stream()
	        .collect(Collectors.toMap(UserAnswer::getQuestionId, q -> q));

	    // Handle userAnswerResult
	    List<UserAnswerResult> userAnswerResults = userAnswers.stream()
	        .filter(userAnswer -> !"group".equals(userAnswer.getType()))
	        .map(userAnswer -> {
	            UserAnswerResult.UserAnswerResultBuilder builder = UserAnswerResult.builder()
	                .questionId(userAnswer.getQuestionId())
	                .questionNum(userAnswer.getQuestionNum())
	                .partNum(userAnswer.getPartNum())
	                .answer(userAnswer.getUserAnswer())
	                .solution(userAnswer.getSolution())
	                .timeSpent(userAnswer.getTimeSpent())
	                .correct(userAnswer.isCorrect())
	                .type(userAnswer.getType())
	                .content(userAnswer.getContent())
                    .difficulty(userAnswer.getDifficulty())
                    .resources(userAnswer.getResources())
                    .transcript(userAnswer.getTranscript())
                    .explanation(userAnswer.getExplanation())
                    .answers(userAnswer.getAnswers())
                    .correctAnswer(userAnswer.getCorrectAnswer())
                    .listTopics(userAnswer.getListTopics());

	                // If it is a subquestion, get info from parent question
	                if ("subquestion".equals(userAnswer.getType()) && userAnswer.getParentId() != null) {
	                    UserAnswer parentUserAnswer = userAnswerMap.get(userAnswer.getParentId());
	                    if (parentUserAnswer != null) {
	                        if (userAnswer.getTranscript() != null) {
	                            builder.transcript(parentUserAnswer.getTranscript());
	                        }
	                        if (userAnswer.getResources().isEmpty()) {
	                            builder.resources(parentUserAnswer.getResources());
	                        }
	                    }
	                }
	            return builder.build();
	        }).collect(Collectors.toList());

	    // Build response
	    ResultSummaryResponse resultSummaryResponse = ResultSummaryResponse.builder()
	        .id(resultId)
	        .testId(result.getTestId())
	        .testName(testName)
	        .totalTime(result.getTotalTime())
	        .totalReadingScore(result.getTotalReadingScore())
	        .totalListeningScore(result.getTotalListeningScore())
	        .totalCorrectAnswer(result.getTotalCorrectAnswer())
	        .totalIncorrectAnswer(result.getTotalIncorrectAnswer())
	        .totalSkipAnswer(result.getTotalSkipAnswer())
	        .type(result.getType())
	        .parts(result.getParts())
	        .build();

	    resultSummaryResponse.setUserAnswers(userAnswerResults);
	    return resultSummaryResponse;
	}
}
