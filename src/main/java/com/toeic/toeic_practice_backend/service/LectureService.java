package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.lecture.LectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.PracticeRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.PracticeRequest.PracticeQuestion;
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

    private MongoTemplate mongoTemplate;

    public PaginationResponse<List<Lecture>> getAllLectures(Pageable pageable, Map<String, Boolean> filterParams) {
        // Tạo một đối tượng Query mới
        Query query = new Query();

        // Áp dụng các bộ lọc từ filterParams
        if (filterParams.containsKey("name") && !filterParams.get("name")) {
            query.fields().exclude("name");
        } else if (filterParams.containsKey("name") && filterParams.get("name")) {
            query.fields().include("name");
        }

        if (filterParams.containsKey("content") && !filterParams.get("content")) {
            query.fields().exclude("content");
        } else if (filterParams.containsKey("content") && filterParams.get("content")) {
            query.fields().include("content");
        }

        if (filterParams.containsKey("practiceQuestions") && !filterParams.get("practiceQuestions")) {
            query.fields().exclude("practiceQuestions");
        } else if (filterParams.containsKey("practiceQuestions") && filterParams.get("practiceQuestions")) {
            query.fields().include("practiceQuestions");
        }

        Boolean orderAsc = filterParams.get("ORDER_ASC");
        Boolean orderDesc = filterParams.get("ORDER_DESC");

        if (orderAsc) {
            query.with(Sort.by(Sort.Direction.ASC));
        } else if (orderDesc) {
            query.with(Sort.by(Sort.Direction.DESC));
        }

        query.with(pageable);

        // Thực thi truy vấn với phân trang
        List<Lecture> lectures = mongoTemplate.find(query, Lecture.class);

        long totalItems = mongoTemplate.count(query.skip(0).limit(0), Lecture.class);

        Page<Lecture> lecturePage = new PageImpl<>(lectures, pageable, totalItems);

        // Trả về kết quả với phân trang
        return PaginationResponse.<List<Lecture>>builder()
            .meta(
                Meta.builder()
                    .current(pageable.getPageNumber() + 1)  // Số trang hiện tại (bắt đầu từ 1)
                    .pageSize(pageable.getPageSize())       // Kích thước trang
                    .totalItems(lecturePage.getTotalElements())  // Tổng số phần tử
                    .totalPages(lecturePage.getTotalPages())  // Tổng số trang
                    .build()
            )
            .result(lecturePage.getContent())  // Danh sách các bài giảng
            .build();
    }

    public Lecture getLectureById(String lectureId) {
        return lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    }

    public Lecture saveLecture(LectureRequest request) {
        List<Topic> topics = topicService.getTopicByIds(request.getTopicIds());
        return lectureRepository.save(
            Lecture
                .builder()
                .name(request.getName())
                .content("")
                .topic(topics)
                .practiceQuestions(new ArrayList<>())
                .build()
        );
    }

    public Lecture saveLectureContent(String lectureId ,String content) {
        Lecture existedLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        existedLecture.setContent(content);
        return lectureRepository.save(existedLecture);
    }

    public Lecture saveLecturePractice(String lectureId, PracticeRequest request) {
        Lecture existedLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        List<Question> questions = request.getPracticeQuestions().stream().map(practiceQuestion -> 
            convertPracticeToQuestion(practiceQuestion)
        ).collect(Collectors.toList());
        List<Question> savedQuestion = questionRepository.saveAll(questions);
        existedLecture.setPracticeQuestions(savedQuestion);
        return lectureRepository.save(existedLecture);
    }

    private Question convertPracticeToQuestion(PracticeQuestion practice) {
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
