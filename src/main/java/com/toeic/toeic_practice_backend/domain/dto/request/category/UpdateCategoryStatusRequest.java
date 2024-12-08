package com.toeic.toeic_practice_backend.domain.dto.request.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryStatusRequest {
	private boolean active;
}
