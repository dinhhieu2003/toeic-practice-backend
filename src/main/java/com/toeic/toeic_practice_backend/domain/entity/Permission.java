package com.toeic.toeic_practice_backend.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
	@Id
	private String id;
	private String name;
	private String apiPath;
	private String method;
	private String module;
}
