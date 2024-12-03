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
	
	public ResultSummaryResponse getResultSummaryById(String resultId) {
	    Result result = resultRepository.findById(resultId)
	        .orElseThrow(() -> new AppException(ErrorCode.RESULT_NOT_FOUND));

	    // get userAnswer
	    List<UserAnswer> userAnswers = result.getUserAnswers();
	    List<String> questionIds = userAnswers.stream()
	        .map(UserAnswer::getQuestionId)
	        .collect(Collectors.toList());

	    // Query question by listId then map it to store
	    Map<String, Question> questionMap = questionService.getQuestionByIds(questionIds)
	        .stream()
	        .collect(Collectors.toMap(Question::getId, q -> q));

	    // Handle userAnswerResult
	    List<UserAnswerResult> userAnswerResults = userAnswers.stream()
	        .filter(userAnswer -> !"group".equals(userAnswer.getType()))
	        .map(userAnswer -> {
	            Question question = questionMap.get(userAnswer.getQuestionId());
	            UserAnswerResult.UserAnswerResultBuilder builder = UserAnswerResult.builder()
	                .questionId(userAnswer.getQuestionId())
	                .questionNum(userAnswer.getQuestionNum())
	                .partNum(userAnswer.getPartNum())
	                .answer(userAnswer.getUserAnswer())
	                .solution(userAnswer.getSolution())
	                .timeSpent(userAnswer.getTimeSpent())
	                .correct(userAnswer.isCorrect())
	                .type(userAnswer.getType());

	            if (question != null) {
	                builder.content(question.getContent())
	                    .difficulty(question.getDifficulty())
	                    .resources(question.getResources())
	                    .transcript(question.getTranscript())
	                    .explanation(question.getExplanation())
	                    .answers(question.getAnswers())
	                    .correctAnswer(question.getCorrectAnswer())
	                    .listTopics(question.getTopic());

	                // If it is a subquestion, get info from parent question
	                if ("subquestion".equals(userAnswer.getType()) && question.getParentId() != null) {
	                    Question parentQuestion = questionMap.get(question.getParentId());
	                    if (parentQuestion != null) {
	                        if (question.getTranscript() != null) {
	                            builder.transcript(parentQuestion.getTranscript());
	                        }
	                        if (question.getResources().isEmpty()) {
	                            builder.resources(parentQuestion.getResources());
	                        }
	                    }
	                }
	            }
	            return builder.build();
	        }).collect(Collectors.toList());

	    // Build response
	    ResultSummaryResponse resultSummaryResponse = ResultSummaryResponse.builder()
	        .id(resultId)
	        .testId(result.getTestId())
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
