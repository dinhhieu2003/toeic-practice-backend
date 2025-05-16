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

import com.toeic.toeic_practice_backend.domain.dto.request.comment.CreateCommentRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.comment.DeleteCommentRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.comment.CommentViewResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.service.CommentService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {
	private final CommentService commentService;
	
	@Operation(summary = "Get root comments", description = "Get paginated root-level comments for a given target.")
	@GetMapping("/root/{targetType}/{targetId}")
	public ResponseEntity<PaginationResponse<List<CommentViewResponse>>> getRootComments(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false, defaultValue = "") String term,
			@RequestParam(required = false) String[] sortBy,
			@RequestParam(required = false) String[] sortDirection,
			@RequestParam(required = false) Boolean active,
			@PathVariable CommentTargetType targetType,
			@PathVariable String targetId) {
		log.info("Get root comment");
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(commentService.getRootComments(pageable, term, sortBy, sortDirection, active,
				targetType, targetId));
	}
	
	@Operation(summary = "Get reply comments", description = "Get paginated reply comments under a parent comment.")
	@GetMapping("/replies/{targetType}/{targetId}/{parentId}")
	public ResponseEntity<PaginationResponse<List<CommentViewResponse>>> getReplyComments(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false, defaultValue = "") String term,
			@RequestParam(required = false) String[] sortBy,
			@RequestParam(required = false) String[] sortDirection,
			@RequestParam(required = false) Boolean active,
			@PathVariable CommentTargetType targetType,
			@PathVariable String targetId,
			@PathVariable String parentId) {
		log.info("Get reply comment with parent id: {}", parentId);
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(commentService.getReplyComments(pageable, term, sortBy, sortDirection, active, targetType, targetId, parentId));
	}
	
	@Operation(summary = "Create a comment", description = "Create a new root or reply comment.")
	@PostMapping("")
	public ResponseEntity<CommentViewResponse> createComment(@RequestBody CreateCommentRequest request) {
		log.info("Create comment");
		return ResponseEntity.ok(commentService.createComment(request));
	}
	
	@Operation(summary = "Toggle like on a comment", description = "Like or unlike a comment.")
	@PutMapping("/toggle-like/{commentId}")
	public ResponseEntity<CommentViewResponse> toggleLike(@PathVariable String commentId) {
		log.info("Toggle like comment");
		return ResponseEntity.ok(commentService.toggleLike(commentId));
	}
	
	@Operation(summary = "Toggle active state of a comment", description = "Activate or deactivate a comment.")
	@PutMapping("/toggle-active/{commentId}")
	public ResponseEntity<CommentViewResponse> toggleActive(@PathVariable String commentId) {
		log.info("Toggle active comment");
		return ResponseEntity.ok(commentService.toggleActiveComment(commentId));
	}
	
	@Operation(summary = "Delete a comment", description = "Mark a comment as deleted with an optional reason.")
	@DeleteMapping("/{commentId}")
	public ResponseEntity<CommentViewResponse> deleteComment(@PathVariable String commentId,
			@RequestBody DeleteCommentRequest request) {
		log.info("Delete comment");
		return ResponseEntity.ok(commentService.deleteComment(commentId, request));
	}
}
