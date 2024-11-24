package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.lecture.LectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.LectureRequest.LecturePractice;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.LectureRepository;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    private final QuestionRepository questionRepository;

    private final TopicService topicService;

    public PaginationResponse<List<Lecture>> getAllLectures(Pageable pageable, Map<String, String> filterParams) {
        Page<Lecture> lecturePage = lectureRepository.findAll(pageable);

        return PaginationResponse.<List<Lecture>>builder()
            .meta(
                Meta.builder()
                    .current(pageable.getPageNumber() + 1)
                    .pageSize(pageable.getPageSize())
                    .totalItems(lecturePage.getTotalElements())
                    .totalPages(lecturePage.getTotalPages())
                    .build()
            )
            .result(lecturePage.getContent())
            .build();
    }

    public Lecture getLectureById(String lectureId) {
        return lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    }

    public Lecture saveLecture(LectureRequest request) {
        List<Topic> topics = topicService.getTopicByIds(request.getTopicIds());
        List<Question> questions = request.getPracticeQuestions().stream().map(practiceQuestion -> 
            convertPracticeToQuestion(practiceQuestion)
        ).collect(Collectors.toList());
        List<Question> savedQuestion = questionRepository.saveAll(questions);
        return lectureRepository.save(
            Lecture
                .builder()
                .name(request.getName())
                .content(request.getContent())
                .topic(topics)
                .practiceQuestions(savedQuestion)
                .build()
        );
    }

    public Question convertPracticeToQuestion(LecturePractice practice) {
        return Question.builder()
            .type(practice.getType())
            .subQuestions(practice.getSubQuestions().size() > 0 ? 
                practice.getSubQuestions().stream().map(sub -> 
                    convertPracticeToQuestion(sub)
                ).collect(Collectors.toList())
                : null
            )
            .content(practice.getContent())
            .topic(topicService.getTopicByIds(practice.getTopicIds()))
            .transcript(practice.getTranscript())
            .explanation(practice.getExplanation())
            .answers(practice.getAnswers())
            .correctAnswer(practice.getCorrectAnswer())
            .build();
    }

    // public Lecture updateLecture(LectureRequest request) {
    //     Lecture existLecture = lectureRepository.findById(request.getId()).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    //     List<Topic> topics = topicService.getTopicByIds(request.getTopicIds());
    //     return lectureRepository.save(existLecture);
    // }
}
