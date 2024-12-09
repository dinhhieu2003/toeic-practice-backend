package com.toeic.toeic_practice_backend.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "categories")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "IDX_format_year", def = "{'format': 1, 'year': 1}")
public class Category extends BaseEntity{
    @Id
    private String id;
    private String format;
    private int year;
}