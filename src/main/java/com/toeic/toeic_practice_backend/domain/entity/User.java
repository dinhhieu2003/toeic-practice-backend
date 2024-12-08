package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class User extends BaseEntity{
	@Id
    private String id;
    private String email;
    private String avatar;
    private String refreshToken;
    @DBRef(lazy = false)
    private Role role;
    private int target;
    private List<String> needUpdateStats = new ArrayList<>(); // list testId bị thay đổi
    private OverallStat overallStat = new OverallStat();
    private List<TopicStat> topicStats = new ArrayList<>();
    private List<SkillStat> skillStats = new ArrayList<>();
    private List<LearningProgress> learningProgress= new ArrayList<>();
    
    public User() {
        skillStats.add(new SkillStat("listening", 0, 0, 0));
        skillStats.add(new SkillStat("reading", 0, 0, 0));
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OverallStat {
    	private int averageListeningScore;
    	private int listeningScoreCount;
    	private int averageReadingScore;
    	private int readingScoreCount;
        private int averageTotalScore;
        private int totalScoreCount;
        private double averageTime;
        private int timeCount;
        private int highestScore;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopicStat {
    	private Topic topic;
    	private int totalCorrect;
    	private int totalIncorrect;
    	private double averageTime;
    	private int timeCount;
    	private int totalTime;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SkillStat {
    	private String skill;	// listening, reading
    	private int totalCorrect;
    	private int totalIncorrect;
    	private int totalTime;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LearningProgress {
    	@DBRef(lazy=false)
    	private Lecture lectureId;
    	private float percent;
    }
}
