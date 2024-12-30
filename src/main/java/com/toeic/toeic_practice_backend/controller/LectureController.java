package com.toeic.toeic_practice_backend.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.data.domain.PageRequest;
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

import com.toeic.toeic_practice_backend.domain.dto.request.lecture.LectureRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.PracticeRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.lecture.UpdateLectureStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLectureStatusResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.service.LectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lectures")
@RequiredArgsConstructor
public class LectureController {
    
    private final LectureService lectureService;

    @GetMapping
    public ResponseEntity<PaginationResponse<List<Lecture>>> getAllLectures(
        @RequestParam(defaultValue = "1") String current,
        @RequestParam(defaultValue = "5") String pageSize,
        @RequestParam(required = false) Boolean info,
        @RequestParam(required = false) Boolean content,
        @RequestParam(required = false) Boolean practice,
        @RequestParam(required = false) Boolean orderAsc,
        @RequestParam(required = false) Boolean orderDesc,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false, defaultValue = "") String search
    ) {
        int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
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
    
    @PostMapping
    public ResponseEntity<Lecture> saveLecture(@RequestBody LectureRequest request) {
        return ResponseEntity.ok(lectureService.saveLecture(request));
    }

    @PostMapping("{lectureId}/saveContent")
    public ResponseEntity<Lecture> saveLectureContent(
        @PathVariable String lectureId,
        @RequestBody String request
    ) {
        return ResponseEntity.ok(lectureService.saveLectureContent(lectureId, request));
    }

    @PostMapping("{lectureId}/savePractice")
    public ResponseEntity<Lecture> saveLecturePractice(
        @PathVariable String lectureId,
        @RequestBody PracticeRequest request
    ) {
        return ResponseEntity.ok(lectureService.saveLecturePractice(lectureId, request));
    }

    @PutMapping("{lectureId}")
    public ResponseEntity<Lecture> updateLecture(
        @PathVariable String lectureId,
        @RequestBody LectureRequest request
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
}
