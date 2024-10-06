package com.toeic.toeic_practice_backend.domain.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "test_results")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TestResult extends BaseEntity {
    @Id
    private String id;
    private int totalScore;
    private String totalTime;
    private int totalReadingScore;
    private int totalListeningScore;
    private int totalCorrectAnswers;
    private int totalIncorrectAnswers;
    private int totalSkipAnswers;
    private String attemptDate;
    private List<Answer> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private String questionId;
        private String questionType;
        private String selectedOption;
        private boolean isCorrect;
        private String solution;
    }
}