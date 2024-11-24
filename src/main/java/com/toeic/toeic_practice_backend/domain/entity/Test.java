package com.toeic.toeic_practice_backend.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "tests")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Test extends BaseEntity{
	@Id
    private String id;
    private String name;
    private int totalUserAttempt;
    private int totalQuestion;
    private int totalScore;
    private int limitTime;
    @JsonIgnore
    private Category category;
}