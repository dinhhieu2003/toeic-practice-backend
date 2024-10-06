package com.toeic.toeic_practice_backend.domain.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "test_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestCategory extends BaseEntity{
    @Id
    private String id;
    private String format;
    private int year;
    private List<String> testIds;
}