package com.toeic.toeic_practice_backend.domain.dto.request.lecture;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.domain.entity.Lecture.PracticeQuestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLecturePracticeRequest {
    private String LectureId;
    private List<PracticeQuestion> practiceQuestions = new ArrayList<>();
}
