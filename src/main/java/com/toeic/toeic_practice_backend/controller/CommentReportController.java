package com.toeic.toeic_practice_backend.controller;

import java.util.List;

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

import com.toeic.toeic_practice_backend.domain.dto.request.commentReport.CreateCommentReportRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.commentReport.UpdateCommentReportRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.CommentReport;
import com.toeic.toeic_practice_backend.service.CommentReportService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportReasonCategory;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportStatus;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/comment-reports")
@RequiredArgsConstructor
@Slf4j
public class CommentReportController {
	private final CommentReportService commentReportService;
	@GetMapping("")
	public ResponseEntity<PaginationResponse<List<CommentReport>>> getCommentReport(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false, defaultValue = "") String term,
			@RequestParam(required = false) String[] sortBy,
			@RequestParam(required = false) String[] sortDirection,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) CommentTargetType commentContextType,
			@RequestParam(required = false) String commentContextId,
			@RequestParam(required = false) CommentReportStatus status,
			@RequestParam(required = false) CommentReportReasonCategory reasonCategory) {
		log.info("Get comment reports");
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(commentReportService.getCommentReports(pageable, term, sortBy, sortDirection, active, 
				commentContextType, commentContextId, status, reasonCategory));
	}
	
	@PostMapping("")
	public ResponseEntity<CommentReport> createCommentReport(@RequestBody CreateCommentReportRequest request) {
		log.info("Create comment report");
		return ResponseEntity.ok(commentReportService.createCommentReport(request));
	}
	
	@PutMapping("/{reportId}")
	public ResponseEntity<CommentReport> updateCommentReport(@PathVariable String reportId,
			@RequestBody UpdateCommentReportRequest request) {
		log.info("Update comment report");
		return ResponseEntity.ok(commentReportService.updateReport(reportId, request));
	}
	
	@DeleteMapping("/{reportId}")
	public ResponseEntity<?> deleteCommentReport(@PathVariable String reportId) {
		log.info("Delete comment report");
		commentReportService.deleteCommentReport(reportId);
		return ResponseEntity.ok(null);
	}
}
