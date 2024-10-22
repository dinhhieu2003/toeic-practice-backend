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
    private List<TestAttempt> testAttemptHistory = new ArrayList<>();
    private List<LearningProgress> learningProgress= new ArrayList<>();
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class TestAttempt {
        private String testId;
        @DBRef(lazy = false)
        private List<Result> results = new ArrayList<>();
        private int totalAttempt;
        private int averageScore;
        private int averageTime;
        private int highestScore;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class LearningProgress {
    	@DBRef(lazy=false)
    	private Course courseId;
    	private float percent;
    }
}
