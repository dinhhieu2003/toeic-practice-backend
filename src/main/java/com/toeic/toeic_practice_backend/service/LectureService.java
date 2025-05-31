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
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.CreatePracticeLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.DeletePracticeLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdateLectureStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdatePracticeLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.CreatePracticeLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.DeletePracticeLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.RandomLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLectureStatusResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdatePracticeLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.domain.entity.Lecture.PracticeQuestion;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.LectureRepository;
import com.toeic.toeic_practice_backend.repository.projectionInterface.LectureNameOnly;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureService {

    private final LectureRepository lectureRepository;

    private final TopicService topicService;

    private final MongoTemplate mongoTemplate;
    
    public String getLectureName(String lectureId) {
    	LectureNameOnly lectureName = lectureRepository.findLectureNameByLectureId(lectureId)
    			.orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    	return lectureName.getName();
    }
    
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
    
    public CreatePracticeLectureResponse addPractice(String lectureId, CreatePracticeLectureRequest request) {
    	log.info("Start: Function add practice");
    	Lecture existLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    	List<PracticeQuestion> practiceQuestions = existLecture.getPracticeQuestions();
    	practiceQuestions.add(request.getPracticeQuestion());
    	existLecture.setPracticeQuestions(practiceQuestions);
    	int totalQuestion = practiceQuestions.size();
    	existLecture.setTotalQuestion(totalQuestion);
    	Lecture updatedLecture = lectureRepository.save(existLecture);
    	log.info("Save practice into database success");
    	CreatePracticeLectureResponse response = CreatePracticeLectureResponse.builder()
    			.lectureId(lectureId)
    			.practiceQuestion(updatedLecture.getPracticeQuestions().get(totalQuestion-1))
    			.build();
    	return response;
    }
    
    public UpdatePracticeLectureResponse updatePractice(String lectureId, UpdatePracticeLectureRequest request) {
    	log.info("Start: Function update practice");
    	Lecture existLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    	List<PracticeQuestion> practiceQuestions = existLecture.getPracticeQuestions();
    	practiceQuestions.set(request.getIndex(), request.getPracticeQuestion());
    	existLecture.setPracticeQuestions(practiceQuestions);
    	Lecture updatedLecture = lectureRepository.save(existLecture);
    	log.info("Save practice into database success");
    	UpdatePracticeLectureResponse response = UpdatePracticeLectureResponse.builder()
    			.index(request.getIndex())
    			.lectureId(updatedLecture.getId())
    			.practiceQuestion(updatedLecture.getPracticeQuestions().get(request.getIndex())).build();
    	log.info("End: Function update practice in lecture");
    	return response;
    }
    
    public DeletePracticeLectureResponse deletePractice(String lectureId, DeletePracticeLectureRequest request) {
    	Lecture existLecture = lectureRepository.findById(lectureId).orElseThrow(()-> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    	List<PracticeQuestion> practiceQuestions = existLecture.getPracticeQuestions();
    	int indexNeedRemove = request.getIndex();
    	if(indexNeedRemove < 0 || indexNeedRemove >= practiceQuestions.size()) {
    		throw new AppException(ErrorCode.INDEX_OUT_BOUND);
    	}
    	practiceQuestions.remove(indexNeedRemove);
    	existLecture.setPracticeQuestions(practiceQuestions);
    	int totalQuestion = practiceQuestions.size();
    	existLecture.setTotalQuestion(totalQuestion);
    	lectureRepository.save(existLecture);
    	DeletePracticeLectureResponse response = DeletePracticeLectureResponse.builder()
    			.lectureId(lectureId)
    			.totalQuestion(totalQuestion).build();
    	return response;
    }
}
