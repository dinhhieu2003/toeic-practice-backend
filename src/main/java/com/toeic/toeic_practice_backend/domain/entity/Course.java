package com.toeic.toeic_practice_backend.domain.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "courses")
@Data
@EqualsAndHashCode(callSuper = false)
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
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Lecture {
    	private String title;
    	private String content;
    	private String description;
    }
}