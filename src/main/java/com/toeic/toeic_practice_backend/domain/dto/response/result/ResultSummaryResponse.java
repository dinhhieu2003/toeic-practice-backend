package com.toeic.toeic_practice_backend.domain.dto.response.result;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultSummaryResponse {
	private String id;
    private String testId;
    private int totalTime;
    private int totalReadingScore;
    private int totalListeningScore;
    private int totalCorrectAnswer;
    private int totalIncorrectAnswer;
    private int totalSkipAnswer;
    private String type;  // practice or fulltest
    private String parts;  // Practice parts
    private List<UserAnswerResult> userAnswers = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserAnswerResult {
    	private String questionId;
    	private int questionNum;
    	private int partNum;
    	private String answer;
    	private String solution;
    	private int timeSpent;
    	private boolean correct;
    }
}
