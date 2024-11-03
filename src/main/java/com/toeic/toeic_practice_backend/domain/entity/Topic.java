package com.toeic.toeic_practice_backend.domain.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "topics")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Topic extends BaseEntity {
	@Id
	private String id;
	private String name;
	private String solution;	// solution for win this topic
	private String overallSkill;		// grammar, vocab
}
