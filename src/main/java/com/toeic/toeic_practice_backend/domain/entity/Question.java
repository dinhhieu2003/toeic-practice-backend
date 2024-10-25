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

@Document(collection = "questions")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BaseEntity{
	@Id
    private String id;
	private String testId;
	private String practiceId;
    private int questionNum;
    private int partNum;
    private String type;  // single, group, or subquestion
    @DBRef(lazy = false)
    private List<Question> subQuestions = new ArrayList<>();
    private String content;
    private int difficulty;
    @DBRef(lazy=false)
    private List<Topic> topic= new ArrayList<>();
    private List<Resource> resources= new ArrayList<>();
    private String transcript;
    private String explanation;
    private List<String> answers= new ArrayList<>();
    private String correctAnswer;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Resource {
        private String type;  // paragraph, image, or audio
        private String content;
    }
}