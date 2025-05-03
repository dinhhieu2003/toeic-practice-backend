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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.lecture.CreateLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.CreateLecturePracticeRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdateLectureStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.RandomLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLectureStatusResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.domain.entity.Lecture.PracticeQuestion;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.LectureRepository;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    private final QuestionRepository questionRepository;

    private final TopicService topicService;

    private final MongoTemplate mongoTemplate;
    
    public List<Lecture> getById(List<String> ids) {
    	return lectureRepository.findByIdIn(ids);
    }
    
    public List<RandomLectureResponse> getRandomLecture(String lectureId) {
    	List<Lecture> lectures = lectureRepository.findRandomLecturesExcludingId(lectureId, 5);
    	return lectures.stream()
                .map(lecture -> new RandomLectureResponse(lecture.getId(), lecture.getName()))
                .collect(Collectors.toList());
    }

    public PaginationResponse<List<Lecture>> getAllLecturesActive(Pageable pageable) {
    	Page<Lecture> lecturePage = lectureRepository.findByIsActiveTrue(pageable);
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
    
    public PaginationResponse<List<Lecture>> getAllLectures(Pageable pageable, Map<String, Boolean> filterParams, String search) {
        // Tạo một đối tượng Query mới
        Query query = new Query();

        // Áp dụng các bộ lọc từ filterParams
        if (filterParams.containsKey("INFO") && !filterParams.get("INFO")) {
            query.fields().exclude("name");
            query.fields().exclude("topic");
        }

        if (filterParams.containsKey("CONTENT") && !filterParams.get("CONTENT")) {
            query.fields().exclude("content");
        }

        if (filterParams.containsKey("PRACTICE") && !filterParams.get("PRACTICE")) {
            query.fields().exclude("practiceQuestions");
        }

        if (filterParams.containsKey("ACTIVE")) {
            Boolean isActive = filterParams.get("ACTIVE");
            System.out.println(isActive);
            query.addCriteria(Criteria.where("isActive").is(isActive));
        }
        
        if (!search.isEmpty()) {
        	query.addCriteria(Criteria.where("name").regex(search, "i"));
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
        return PaginationUtils.buildPaginationResponse(pageable, lecturePage);
    }

    public Lecture getLectureById(String lectureId, Map<String, Boolean> filterParams) {
        Query query = new Query();

        // Áp dụng các bộ lọc từ filterParams
        if (filterParams.containsKey("INFO") && !filterParams.get("INFO")) {
            query.fields().exclude("name");
            query.fields().exclude("topic");
        }

        if (filterParams.containsKey("CONTENT") && !filterParams.get("CONTENT")) {
            query.fields().exclude("content");
        }

        if (filterParams.containsKey("PRACTICE") && !filterParams.get("PRACTICE")) {
            query.fields().exclude("practiceQuestions");
        }

        Lecture lecture = mongoTemplate.findOne(query.addCriteria(Criteria.where("_id").is(lectureId)), Lecture.class);
        
        if (lecture == null) {
            throw new AppException(ErrorCode.LECTURE_NOT_FOUND);
        }

        return lecture;
    }

    public Lecture saveLecture(CreateLectureRequest request) {
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

    public Lecture saveLecturePractice(String lectureId, CreateLecturePracticeRequest request) {
        Lecture existedLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        List<PracticeQuestion> practiceQuestions = request.getPracticeQuestions();
        existedLecture.setPracticeQuestions(practiceQuestions);
        existedLecture.setTotalQuestion(practiceQuestions.size());
        return lectureRepository.save(existedLecture);
    }

    public Lecture updateLecture(String lectureId, CreateLectureRequest request) {
        Lecture existLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        List<Topic> topics = topicService.getTopicByIds(request.getTopicIds());
        existLecture.setName(request.getName());
        existLecture.setContent(request.getContent());
        existLecture.setTopic(topics);
        return lectureRepository.save(existLecture);
    }

    public void deleteLecturePractice(String practiceId) {
        Lecture lecture = lectureRepository.findById(practiceId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        lectureRepository.delete(lecture);
    }
    
    public UpdateLectureStatusResponse updateLectureStatus(String lectureId, UpdateLectureStatusRequest updateLectureStatusRequest) {
    	Lecture existLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    	existLecture.setActive(updateLectureStatusRequest.isActive());
    	Lecture newLecture = lectureRepository.save(existLecture);
    	UpdateLectureStatusResponse updateLectureStatusResponse = new UpdateLectureStatusResponse();
    	updateLectureStatusResponse.setId(newLecture.getId());
    	updateLectureStatusResponse.setName(newLecture.getName());
    	updateLectureStatusResponse.setActive(newLecture.isActive());
    	return updateLectureStatusResponse;
    }
    
    public List<Lecture> getLecturesByIdIn(List<String> lectureIds) {
    	return lectureRepository.findLectureByIdIn(lectureIds);
    }
}
