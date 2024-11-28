package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "results")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result extends BaseEntity {
	@Id
    private String id;
    private String testId;
    private String userId;
    private int totalTime;
    private int totalReadingScore;
    private int totalListeningScore;
    private int totalCorrectAnswer;
    private int totalIncorrectAnswer;
    private int totalSkipAnswer;
    private String type;  // practice or fulltest
    private String parts;  // Practice parts
    private List<UserAnswer> userAnswers = new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserAnswer {
        private String questionId;
        private String parentId;	// id of group
        private List<Topic> listTopics = new ArrayList<>();
        private String userAnswer;
        private String solution;
        private boolean isCorrect;
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
        private List<UserAnswer> subUserAnswer = new ArrayList<>();
    }
}