package com.toeic.toeic_practice_backend.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "questions")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Indexed
    private String id;
	@Indexed
	private String testId;
	private String parentId;	// id of group (question)
    private int questionNum;
    private int partNum;
    private String type;  // single, group, or subquestion
    @DBRef(lazy=true)
    private List<Question> subQuestions = new ArrayList<>();
    private String content;
    private int difficulty;
    private List<Topic> topic= new ArrayList<>();
    private List<Resource> resources= new ArrayList<>();
    private String transcript;
    private String explanation;
    private List<String> answers= new ArrayList<>();
    private String correctAnswer;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Resource implements Serializable {
		private static final long serialVersionUID = 1L;
		private String type;  // paragraph, image, or audio
        private String content;
    }
}