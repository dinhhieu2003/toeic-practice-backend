package com.toeic.toeic_practice_backend.domain.dto.request.lecture;

import com.toeic.toeic_practice_backend.domain.entity.Lecture.PracticeQuestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePracticeLectureRequest {
	private PracticeQuestion practiceQuestion;
}
