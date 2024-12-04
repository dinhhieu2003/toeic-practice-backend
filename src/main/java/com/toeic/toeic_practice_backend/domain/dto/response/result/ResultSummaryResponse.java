package com.toeic.toeic_practice_backend.domain.dto.response.result;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;

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
    private String testName;
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
        private List<Topic> listTopics = new ArrayList<>();
        private String answer;
        private String solution;
        private boolean correct;
        private int timeSpent;
        private int questionNum;
        private int partNum;
        private String type;
        private String content;
        private int difficulty;
        private List<Resource> resources= new ArrayList<>();
        private String transcript;
        private String explanation;
        private List<String> answers= new ArrayList<>();
        private String correctAnswer;
    }
}
