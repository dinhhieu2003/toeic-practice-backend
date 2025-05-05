package com.toeic.toeic_practice_backend.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.lecture.CreateLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.CreatePracticeLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.DeletePracticeLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.CreateLecturePracticeRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdateLecturePercentRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdateLectureStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdatePracticeLectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.CreatePracticeLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.DeletePracticeLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.LectureCardResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.RandomLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLecturePercentResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLectureStatusResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdatePracticeLectureResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.service.LectureService;
import com.toeic.toeic_practice_backend.service.LectureUserService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/lectures")
@RequiredArgsConstructor
public class LectureController {
    
    private final LectureService lectureService;
    private final LectureUserService lectureUserService;

    @GetMapping("")
    public ResponseEntity<PaginationResponse<List<Lecture>>> getAllLectures(
        @RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
        @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
        @RequestParam(required = false) Boolean info,
        @RequestParam(required = false) Boolean content,
        @RequestParam(required = false) Boolean practice,
        @RequestParam(required = false) Boolean orderAsc,
        @RequestParam(required = false) Boolean orderDesc,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false, defaultValue = "") String search
    ) {
        Pageable pageable = PaginationUtils.createPageable(current, pageSize);
        Map<String, Boolean> filterParams = new HashedMap<>();
        filterParams.put("INFO", info != null ? info : false);
        filterParams.put("CONTENT", content != null ? content : false);
        filterParams.put("PRACTICE", practice != null ? practice : false);
        filterParams.put("ORDER_ASC", orderAsc != null ? orderAsc : false);
        filterParams.put("ORDER_DESC", orderDesc != null ? orderDesc : false);
        if (active != null) {
            filterParams.put("ACTIVE", active);
        }
        return ResponseEntity.ok(lectureService.getAllLectures(pageable, filterParams, search));
    }
    
    // Get lecture cards with percent for client
    @GetMapping("client")
    public ResponseEntity<PaginationResponse<List<LectureCardResponse>>> getAllLecturesForClient(
    		@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
        @RequestParam(required = false) Boolean info,
        @RequestParam(required = false) Boolean content,
        @RequestParam(required = false) Boolean practice,
        @RequestParam(required = false) Boolean orderAsc,
        @RequestParam(required = false) Boolean orderDesc,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false, defaultValue = "") String search
     ) {
        Pageable pageable = PaginationUtils.createPageable(current, pageSize);
        Map<String, Boolean> filterParams = new HashedMap<>();
        filterParams.put("INFO", info != null ? info : true);
        filterParams.put("CONTENT", content != null ? content : false);
        filterParams.put("PRACTICE", practice != null ? practice : false);
        filterParams.put("ORDER_ASC", orderAsc != null ? orderAsc : false);
        filterParams.put("ORDER_DESC", orderDesc != null ? orderDesc : false);
        if (active != null) {
            filterParams.put("ACTIVE", active);
        }
        return ResponseEntity.ok(lectureUserService.getAllLectures(pageable, filterParams, search));
    }
    
    
    @GetMapping("{lectureId}/random")
    public ResponseEntity<List<RandomLectureResponse>> getRandomLecture(@PathVariable String lectureId) {
    	return ResponseEntity.ok(lectureService.getRandomLecture(lectureId));
    }

    @GetMapping("{lectureId}")
    public ResponseEntity<Lecture> getLectureById(
        @PathVariable String lectureId,
        @RequestParam(required = false) Boolean info,
        @RequestParam(required = false) Boolean content,
        @RequestParam(required = false) Boolean practice
    ) {
        Map<String, Boolean> filterParams = new HashedMap<>();
        filterParams.put("INFO", info != null ? info : false);
        filterParams.put("CONTENT", content != null ? content : false);
        filterParams.put("PRACTICE", practice != null ? practice : false);
        return ResponseEntity.ok(lectureService.getLectureById(lectureId, filterParams));
    }
    
    @PostMapping("")
    public ResponseEntity<Lecture> saveLecture(@RequestBody CreateLectureRequest request) {
        return ResponseEntity.ok(lectureService.saveLecture(request));
    }

    @PostMapping("{lectureId}/saveContent")
    public ResponseEntity<Lecture> saveLectureContent(
        @PathVariable String lectureId,
        @RequestBody String request
    ) {
        return ResponseEntity.ok(lectureService.saveLectureContent(lectureId, request));
    }
    
    @PostMapping("{lectureId}/practice")
    public ResponseEntity<CreatePracticeLectureResponse> addPractice(@PathVariable String lectureId, @RequestBody CreatePracticeLectureRequest request) {
    	return ResponseEntity.ok(lectureService.addPractice(lectureId, request));
    }
    
    @PutMapping("{lectureId}/practice")
    public ResponseEntity<UpdatePracticeLectureResponse> updatePractice(@PathVariable String lectureId, @RequestBody UpdatePracticeLectureRequest request) {
    	return ResponseEntity.ok(lectureService.updatePractice(lectureId, request));
    }
    
    @DeleteMapping("{lectureId}/practice")
    public ResponseEntity<DeletePracticeLectureResponse> deletePractice(@PathVariable String lectureId, @RequestBody DeletePracticeLectureRequest request) {
    	return ResponseEntity.ok(lectureService.deletePractice(lectureId, request));
    }

    @PutMapping("{lectureId}")
    public ResponseEntity<Lecture> updateLecture(
        @PathVariable String lectureId,
        @RequestBody CreateLectureRequest request
    ) {
        return ResponseEntity.ok(lectureService.updateLecture(lectureId, request));
    }

    @DeleteMapping("{lectureId}")
    public ResponseEntity<?> deleteLecturePractice(
        @PathVariable String lectureId
    ) {
        lectureService.deleteLecturePractice(lectureId);
        return ResponseEntity.ok(null);
    }
    
    @PutMapping("{lectureId}/status")
    public ResponseEntity<UpdateLectureStatusResponse> updateLectureStatus(
    		@PathVariable String lectureId,
    		@RequestBody UpdateLectureStatusRequest updateLectureStatusRequest) {
    	return ResponseEntity.ok(lectureService.updateLectureStatus(lectureId, updateLectureStatusRequest));
    }
    
    // Update lecture percent for user
    @PutMapping("{lectureId}/percent")
    public ResponseEntity<UpdateLecturePercentResponse> updateLecturePercent(
    		@PathVariable String lectureId, 
    		@RequestBody UpdateLecturePercentRequest percent) {
    	return ResponseEntity.ok(lectureUserService.updateLecturePercent(lectureId, percent.getPercent()));
    }
}
