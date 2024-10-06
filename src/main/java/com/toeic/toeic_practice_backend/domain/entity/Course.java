package com.toeic.toeic_practice_backend.domain.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity{
    @Id
    private String id;
    private String name;
    private String topic;
    private String format;
    private String difficulty;
    private Lecture lecture;
    private String content;
    private String description;
    private List<String> questionIds;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Lecture {
    	private String title;
    	private String content;
    	private String description;
    }
}