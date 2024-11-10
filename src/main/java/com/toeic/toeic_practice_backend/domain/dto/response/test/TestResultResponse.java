package com.toeic.toeic_practice_backend.domain.dto.response.test;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultResponse {
    private String id;
    private String testId;
    private int totalTime;
    private int totalReadingScore;
    private int totalListeningScore;
    private int totalCorrectAnswer;
    private int totalIncorrectAnswer;
    private int totalSkipAnswer;
    private String type;  // practice or fulltest
    private String parts;  // Practice parts
    private List<UserAnswer> userAnswers = new ArrayList<>();
}
