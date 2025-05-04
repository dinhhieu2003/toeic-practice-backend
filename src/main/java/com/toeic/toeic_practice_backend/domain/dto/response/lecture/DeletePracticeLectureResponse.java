package com.toeic.toeic_practice_backend.domain.dto.response.lecture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletePracticeLectureResponse {
	private String lectureId;
	private int totalQuestion;
}
