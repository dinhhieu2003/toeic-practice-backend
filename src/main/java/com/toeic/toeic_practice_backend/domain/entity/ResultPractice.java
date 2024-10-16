package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "results_practice")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ResultPractice {
	@Id
	private String id;
	private String practiceId;
	private String userId;
	private int totalCorrectAnswer;
	private int totalIncorrectAnswer;
	private int totalSkipAnswer;
	private List<UserAnswer> userAnswers = new ArrayList<>();
	
	@Data
    @AllArgsConstructor
    @NoArgsConstructor
    class UserAnswer {
        private String questionId;
        private String answer;
    }
}
