package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private HashMap<String, Integer> learningProgress= new HashMap<>();	// {lectureId: percent}
    private HashSet<String> testHistory = new HashSet<>();	// list testId done
    
    public User() {
        skillStats.add(new SkillStat("listening", 0, 0, 0));
        skillStats.add(new SkillStat("reading", 0, 0, 0));
        target = 10;
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
        
        public void updateStats(int listeningScoreFromScoreBoard, 
        		int readingScoreFromScoreBoard, int totalSeconds) {
        	// listening, reading score
    		int currentListeningCount = this.listeningScoreCount;
    		int currentReadingCount = this.readingScoreCount;
    		int currentAvgListeningScore = this.averageListeningScore;
    		int currentAvgReadingScore = this.averageReadingScore;
    		int listeningScore = listeningScoreFromScoreBoard;
    		int readingScore = readingScoreFromScoreBoard;
    		int newAvgListeningScore = 
    				((currentAvgListeningScore * currentListeningCount) + listeningScore) / (currentListeningCount + 1);
    		int newAvgReadingScore = 
    				((currentAvgReadingScore * currentReadingCount) + readingScore) / (currentReadingCount + 1);
    		this.averageListeningScore = newAvgListeningScore;
    		this.averageReadingScore = newAvgReadingScore;
    		this.listeningScoreCount = currentListeningCount + 1;
    		this.readingScoreCount = currentReadingCount + 1;
    		// time
    		int currentTimeCount = this.timeCount;
    		double currentAvgTime = this.averageTime;
    		double newAvgTime = 
    				((currentAvgTime * currentTimeCount) + totalSeconds) / (currentTimeCount + 1);
    		this.timeCount = currentTimeCount + 1;
    		this.averageTime = newAvgTime;
    		
    		// total
    		int totalScore = listeningScore + readingScore;
    		int currentTotalScoreCount = this.totalScoreCount;
    		int currentAvgTotalScore = this.averageTotalScore;
    		int newAvgTotalScore =
    				((currentAvgTotalScore * currentTotalScoreCount) + totalScore) / (currentTotalScoreCount + 1);
    		this.averageTotalScore = newAvgTotalScore;
    		this.totalScoreCount = currentTotalScoreCount + 1;
    		// highest
    		if(totalScore > this.highestScore) {
    			this.highestScore = totalScore;
    		}
        }
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
    	
    	public void updateStats(boolean isCorrect, boolean isSkip, int timeSpent) {
    		if(isCorrect) {
    			this.totalCorrect++;
    		} else if(!isSkip) {
    			this.totalIncorrect++;
    		}
    		double avgTime = (this.averageTime * this.timeCount + timeSpent) /(this.timeCount + 1);
    		this.averageTime = avgTime;
    		this.timeCount++;
    		this.totalTime += timeSpent;
    	}
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
    	
    	public void updateStats(boolean isCorrect, boolean isSkip, int timeSpent) {
    		if(isCorrect) {
    			this.totalCorrect++;
    		} else if(!isSkip) {
    			this.totalIncorrect++;
    		}
    		this.totalTime += timeSpent;
    	}
    }
}
