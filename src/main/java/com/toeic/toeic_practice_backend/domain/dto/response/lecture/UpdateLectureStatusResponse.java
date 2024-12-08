package com.toeic.toeic_practice_backend.domain.dto.response.lecture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLectureStatusResponse {
	private String id;
	private String name;
	private boolean isActive;
}
