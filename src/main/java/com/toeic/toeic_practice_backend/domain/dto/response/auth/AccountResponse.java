package com.toeic.toeic_practice_backend.domain.dto.response.auth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toeic.toeic_practice_backend.domain.entity.Role;
import com.toeic.toeic_practice_backend.domain.entity.User.LearningProgress;
import com.toeic.toeic_practice_backend.domain.entity.User.OverallStat;
import com.toeic.toeic_practice_backend.domain.entity.User.SkillStat;
import com.toeic.toeic_practice_backend.domain.entity.User.TopicStat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
	private String id;
    private String email;
    private String avatar;
    private Role role;
    private int target;
    private OverallStat overallStat;
    private List<TopicStat> topicStats = new ArrayList<>();
    private List<SkillStat> skillStats = new ArrayList<>();
    @JsonIgnore
    private List<LearningProgress> learningProgress= new ArrayList<>();
    private List<ResultOverview> results;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultOverview {
    	private Instant createdAt;
    	private String testId;
    	private String resultId;
    	private String testName;
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
}
