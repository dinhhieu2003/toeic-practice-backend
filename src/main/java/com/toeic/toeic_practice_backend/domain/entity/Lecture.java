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

@Document(collection = "lectures")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Lecture extends BaseEntity{
	@Id
    private String id;
    private String name;
    private String content;
    @DBRef
    private List<Topic> topic = new ArrayList<>();
    @DBRef(lazy=false)
    private List<Practice> practices;
}