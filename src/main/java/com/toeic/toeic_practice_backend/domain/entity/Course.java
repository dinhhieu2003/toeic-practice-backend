package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
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
    private List<String> topic = new ArrayList<>();  // Array of topic names
    private String format;
    private int difficulty;
    private Lecture lecture;
    private Assignment assignment;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Assignment {
        private int required;
        private int totalQuestion;
        private List<String> questionIds;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Lecture {
        private String title;
        private String content;
        private String description;
    }
}