package com.toeic.toeic_practice_backend.domain.dto.response.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryStatusResponse {
	private String id;
	private String format;
	private int year;
	private boolean isActive;
}
