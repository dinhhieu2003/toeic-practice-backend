package com.toeic.toeic_practice_backend.domain.dto.response.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTestCardResponse {
	private String id;
	private String format;
	private int year;
	private String name;
}
