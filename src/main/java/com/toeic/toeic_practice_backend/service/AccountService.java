package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.auth.AccountResponse.ResultOverview;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Test;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final ResultService resultService;
	private final TestService testService;
	public List<ResultOverview> getResultOverview(String userId) {
		List<Result> results = resultService.getResultsByUserIdWithoutUserAnswer(userId);
		if(results.size() == 0) {
			return new ArrayList<ResultOverview>();
		}
		Set<String> testIds = new HashSet<>();
		for(Result result: results) {
			testIds.add(result.getTestId());
		}
		List<Test> tests = testService.getTestByIdIn(new ArrayList<>(testIds));
		HashMap<String, String> testNameMap = new HashMap<>();
		for(Test test: tests) {
			testNameMap.put(test.getId(), test.getName());
		}
		
		List<ResultOverview> listResultOverview = new ArrayList<>(); 
		for(Result result: results) {
			int totalQuestion = (int) result.getUserAnswers()
					.stream()
					.filter(userAnswer -> !"group".equals(userAnswer.getType()))
					.count();
			String score = result.getTotalCorrectAnswer() + "/" + totalQuestion;
			ResultOverview resultOverview = ResultOverview
					.builder()
					.createdAt(result.getCreatedAt())
					.testId(result.getTestId())
					.resultId(result.getId())
					.testName(testNameMap.get(result.getTestId()))
					.result(score)
					.totalTime(result.getTotalTime())
				    .totalReadingScore(result.getTotalReadingScore())
				    .totalListeningScore(result.getTotalListeningScore())
				    .totalCorrectAnswer(result.getTotalCorrectAnswer())
				    .totalIncorrectAnswer(result.getTotalIncorrectAnswer())
				    .totalSkipAnswer(result.getTotalSkipAnswer())
				    .type(result.getType())
				    .parts(result.getParts())
				    .build();
			listResultOverview.add(resultOverview);
		}
		return listResultOverview;	
	}
}
