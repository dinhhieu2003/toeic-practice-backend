package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "results")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
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
    private List<Integer> parts = new ArrayList<>();  // Practice parts
    private List<UserAnswer> userAnswers = new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class UserAnswer {
        private String questionId;
        private String answer;
        private String solution;
    }
}