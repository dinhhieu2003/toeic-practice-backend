package com.toeic.toeic_practice_backend.domain.dto.response.test;

import java.time.Instant;
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
public class TestInfoResponse {
	private String id;
    private String name;
    private int totalUserAttempt;
    private int totalQuestion;
    private int totalScore;
    private int limitTime;
    @Builder.Default
    private List<ResultOverview> resultsOverview = new ArrayList<>();
    @Builder.Default
    private List<TopicOverview> topicsOverview = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultOverview {
    	private String resultId;
    	private Instant createdAt;
    	private String result;	// x/200, x/30
    	private int totalTime;
        private int totalReadingScore;
        private int totalListeningScore;
        private int totalCorrectAnswer;
        private int totalIncorrectAnswer;
        private int totalSkipAnswer;
        private String type;  // practice or fulltest
        private String parts;  // Practice parts
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicOverview {
    	private int partNum;
    	private List<String> topicNames;
    }
}
