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

@Document(collection = "lectures")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture extends BaseEntity{
	@Id
    private String id;
    private String name;
    private String content;
    @DBRef(lazy=true)
    private List<Topic> topic = new ArrayList<>();
    private List<PracticeQuestion> practiceQuestions = new ArrayList<>();
    private int totalQuestion = 0;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PracticeQuestion {
    	private String content;
    	private List<Resource> resources = new ArrayList<>();
    	private String transcript;
    	private String explanation;
        private List<String> answers= new ArrayList<>();
        private String correctAnswer;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Resource {
    	private String type;
    	private String content;
    }
}