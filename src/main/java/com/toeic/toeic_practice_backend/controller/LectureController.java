package com.toeic.toeic_practice_backend.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.lecture.LectureRequest;
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
        @RequestParam(required = false) String type
    ) {
        int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
        Map<String, String> filterParams = new HashedMap<>();
        return ResponseEntity.ok(lectureService.getAllLectures(pageable, filterParams));
    }

    @GetMapping("{lectureId}")
    public ResponseEntity<Lecture> getLectureById(
        @PathVariable String lectureId
    ) {
        return ResponseEntity.ok(lectureService.getLectureById(lectureId));
    }
    
    @PostMapping
    public ResponseEntity<Lecture> saveLecture(@RequestBody LectureRequest request) {
        return ResponseEntity.ok(lectureService.saveLecture(request));
    }
}
