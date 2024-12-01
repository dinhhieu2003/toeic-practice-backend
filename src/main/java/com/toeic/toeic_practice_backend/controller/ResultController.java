package com.toeic.toeic_practice_backend.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.result.ResultSummaryResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultResponse;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;
import com.toeic.toeic_practice_backend.service.ResultService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
public class ResultController {
    
    private final ResultService resultService;

    @GetMapping
    public ResponseEntity<PaginationResponse<List<TestResultResponse>>> getAllResults(
        @RequestParam(defaultValue = "1") String current,
        @RequestParam(defaultValue = "5") String pageSize,
        @RequestParam(required = false) String type
    ) {
        int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
        Map<String, String> filterParams = new HashedMap<>(); 
		filterParams.put("TYPE", type);
        return ResponseEntity.ok(resultService.getAllResults(pageable, filterParams));
    } 
    
    @GetMapping("{resultId}")
    public ResponseEntity<ResultSummaryResponse> getResultById(
        @PathVariable String resultId
    ) {
        return ResponseEntity.ok(resultService.getById(resultId));
    }
    
    @GetMapping("mobile/{resultId}")
    public ResponseEntity<Result> getResultByIdMobile(
            @PathVariable String resultId
        ) {
            return ResponseEntity.ok(resultService.getByIdMobile(resultId));
        }
    
    @GetMapping("{resultId}/review")
    public ResponseEntity<List<UserAnswer>> getReviewByResultId(
        @PathVariable String resultId
    ) {
        return ResponseEntity.ok(resultService.getReview(resultId));
    }
}
