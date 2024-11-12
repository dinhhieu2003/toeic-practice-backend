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
@NoArgsConstructor
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
    private OverallStat overallStat;
    private List<SkillStat> skillStats = new ArrayList<>();
    private List<LearningProgress> learningProgress= new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OverallStat {
    	private int averageListeningScore;
    	private int averageReadingScore;
        private int averageTotalScore;
        private double averageTime;
        private int highestScore;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SkillStat {
    	@DBRef
    	private Topic topic;
    	private String testSkill;	// listening, reading
    	private String overallSkill;	// grammar, vocab
    	private int totalCorrect;
    	private int totalIncorrect;
    	private double averageTime;
    	private double totalTime;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LearningProgress {
    	@DBRef(lazy=false)
    	private Course courseId;
    	private float percent;
    }
}
