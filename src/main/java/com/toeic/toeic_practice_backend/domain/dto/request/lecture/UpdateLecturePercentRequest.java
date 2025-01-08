package com.toeic.toeic_practice_backend.domain.dto.request.lecture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLecturePercentRequest {
	private int percent;
}
