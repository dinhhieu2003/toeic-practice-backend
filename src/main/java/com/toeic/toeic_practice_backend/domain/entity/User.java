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
	private List<Attempt> testAttemptHistory = new ArrayList<>();
    private List<Learning> learningProgress = new ArrayList<>();
    @DBRef(lazy = false)
    private Role role;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Attempt {
		private String testId;
		private int totalAttempt;
		private float averageScore;
		private float averageTime;
		private float highestScore;
		private String resultId;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Learning {
		private String courseId;
		private boolean isCompleted;
	}
}
