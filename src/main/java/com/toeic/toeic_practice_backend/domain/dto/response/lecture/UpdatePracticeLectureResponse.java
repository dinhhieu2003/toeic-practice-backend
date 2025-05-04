package com.toeic.toeic_practice_backend.domain.dto.response.lecture;

import com.toeic.toeic_practice_backend.domain.entity.Lecture.PracticeQuestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePracticeLectureResponse {
	private String lectureId;
	private int index;
	private PracticeQuestion practiceQuestion;
}
