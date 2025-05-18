package com.toeic.toeic_practice_backend.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.toeic_practice_backend.domain.dto.request.comment.CreateCommentRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.comment.DeleteCommentRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.ApiResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.comment.CommentClassificationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.comment.CommentViewResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Comment;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.CommentMapper;
import com.toeic.toeic_practice_backend.repository.CommentRepository;
import com.toeic.toeic_practice_backend.repository.specification.CommentSpecification;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.DeleteReasonTagComment;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
	private final MongoTemplate mongoTemplate;
	private final CommentRepository commentRepository;
	private final CommentMapper commentMapper;
	private final UserService userService;
	private final CommentClassificationService commentClassificationService;
	
	public PaginationResponse<List<Comment>> getComments(Pageable pageable, String term, String[] sortBy, String[] sortDirection, Boolean active) {
		if(sortBy == null || sortBy.length == 0) {
			sortBy = new String[] {"createdAt"};
		}
		
		if(sortDirection == null || sortDirection.length == 0) {
			sortDirection = new String[] {"desc"};
		}
		String parentId = null;
		CommentTargetType commentTargetType = null;
		String targetId = null;
		boolean filteredByParentId = false;
		CommentSpecification spec = new CommentSpecification(term, sortBy, sortDirection, active,
				commentTargetType, targetId, parentId, filteredByParentId);
		
		Query query = spec.buildQuery(pageable);
		List<Comment> comments = mongoTemplate.find(query, Comment.class);
		long totalItems = mongoTemplate.count(query.skip(0).limit(0), Comment.class);
		Page<Comment> pageData = new PageImpl<>(comments, pageable, totalItems);
		PaginationResponse<List<Comment>> response = 
				PaginationUtils.buildPaginationResponse(pageable, pageData);
		return response;
	}
	
	public PaginationResponse<List<CommentViewResponse>> getRootComments(Pageable pageable, String term, String[] sortBy, String[] sortDirection, Boolean active,
			CommentTargetType commentTargetType, String targetId) {
		if(sortBy == null || sortBy.length == 0) {
			sortBy = new String[] {"createdAt"};
		}
		
		if(sortDirection == null || sortDirection.length == 0) {
			sortDirection = new String[] {"desc"};
		}
		String parentId = null;
		boolean filteredByParentId = true;
		CommentSpecification spec = new CommentSpecification(term, sortBy, sortDirection, active,
				commentTargetType, targetId, parentId, filteredByParentId);
		
		// current user
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userId = null;
        Optional<User> user = userService.getUserByEmailWithoutStat(username);
        if(user.isPresent()) {
        	userId = user.get().getId();
        }
		
		Query query = spec.buildQuery(pageable);
		List<Comment> comments = mongoTemplate.find(query, Comment.class);
		long totalItems = mongoTemplate.count(query.skip(0).limit(0), Comment.class);
		List<CommentViewResponse> commentsViewResponse = commentMapper.toCommentViewResponseList(comments, userId);
		
		Page<CommentViewResponse> pageData = new PageImpl<>(commentsViewResponse, pageable, totalItems);
		PaginationResponse<List<CommentViewResponse>> response = 
				PaginationUtils.buildPaginationResponse(pageable, pageData);
		return response;
	}
	
	public PaginationResponse<List<CommentViewResponse>> getReplyComments(Pageable pageable, String term, String[] sortBy, String[] sortDirection, Boolean active,
			CommentTargetType commentTargetType, String targetId, String parentId) {
		if(sortBy == null || sortBy.length == 0) {
			sortBy = new String[] {"createdAt"};
		}
		
		if(sortDirection == null || sortDirection.length == 0) {
			sortDirection = new String[] {"desc"};
		}
		
		boolean filteredByParentId = true;
		CommentSpecification spec = new CommentSpecification(term, sortBy, sortDirection, active,
				commentTargetType, targetId, parentId, filteredByParentId);
		
		// current user
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userId = null;
        Optional<User> user = userService.getUserByEmailWithoutStat(username);
        if(user.isPresent()) {
        	userId = user.get().getId();
        }
		
		Query query = spec.buildQuery(pageable);
		List<Comment> comments = mongoTemplate.find(query, Comment.class);
		long totalItems = mongoTemplate.count(query.skip(0).limit(0), Comment.class);
		List<CommentViewResponse> commentsViewResponse = commentMapper.toCommentViewResponseList(comments, userId);
		
		Page<CommentViewResponse> pageData = new PageImpl<>(commentsViewResponse, pageable, totalItems);
		PaginationResponse<List<CommentViewResponse>> response = 
				PaginationUtils.buildPaginationResponse(pageable, pageData);
		return response;
	}

	public CommentViewResponse createComment(CreateCommentRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = authentication.getName();
	    User user = userService.getUserByEmailWithoutStat(email)
	        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
	    HashSet<String> mentionedUserIds = new HashSet<>();
	    if(request.getMentionedUserIds() != null) {
	    	mentionedUserIds = new HashSet<>(request.getMentionedUserIds());
	    }
	    // check level of comment via parent comment
	    int level = 0;
	    String rootId = null;
	    Comment parentComment = null;
	    // direct reply count for parent if have
	    int directReplyCountsParent = 0;
	    if(request.getParentId() != null) {
	    	parentComment = commentRepository.findById(request.getParentId())
	    			.orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
	    	directReplyCountsParent = parentComment.getDirectReplyCount() + 1;
	    	parentComment.setDirectReplyCount(directReplyCountsParent);
	    	level = Math.min(parentComment.getLevel() + 1, 2);
	    	if(level == 1) {
	    		rootId = parentComment.getId();
	    	}
	    	if(level == 2) {
	    		rootId = parentComment.getRootId();
	    	}
	    }
	    Comment comment = Comment.builder()
	            .content(request.getContent())
	            .targetType(request.getTargetType())
	            .targetId(request.getTargetId())
	            .parentId(request.getParentId())
	            .rootId(rootId)
	            .mentionedUserIds(mentionedUserIds)
	            .userId(user.getId())
	            .userDisplayName(user.getEmail())
	            .userAvatarUrl(user.getAvatar())
	            .likedUserIds(new HashSet<>())
	            .likeCounts(0)
	            .directReplyCount(0)
	            .level(level)
	            .deleted(false)
	            .build();
	    comment.setActive(true);
	    Comment createdComment = commentRepository.save(comment);
	   
	    commentClassificationService.checkComment(createdComment);
	    
	    // Save direct reply count for parent if have
	    if(parentComment != null) {
	    	commentRepository.save(parentComment);
	    }
	    CommentViewResponse response = commentMapper.toCommentViewResponse(createdComment, user.getId());
	    return response;
	}
	
	public CommentViewResponse toggleLike(String commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = authentication.getName();
	    User user = userService.getUserByEmailWithoutStat(email)
	        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
	    Comment comment = commentRepository.findById(commentId)
	    		.orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
	    HashSet<String> likedUserIds = comment.getLikedUserIds();
	    // unlike
	    if(likedUserIds.contains(user.getId())) {
	    	likedUserIds.remove(user.getId());
	    	comment.setLikedUserIds(likedUserIds);
	    	comment.setLikeCounts(comment.getLikeCounts() - 1);
	    } else {
	    	// like
	    	likedUserIds.add(user.getId());
	    	comment.setLikedUserIds(likedUserIds);
	    	comment.setLikeCounts(comment.getLikeCounts() + 1);
	    }
	    
	    Comment savedComment = commentRepository.save(comment);
	    
	    CommentViewResponse response = commentMapper.toCommentViewResponse(savedComment, user.getId());
	    return response;
	}
	
	public CommentViewResponse toggleActiveComment(String commentId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = authentication.getName();
	    User user = userService.getUserByEmailWithoutStat(email)
	            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

	    Comment comment = commentRepository.findById(commentId)
	            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
	    
	    boolean active = !comment.isActive();
	    comment.setActive(active);
	    Comment savedComment = commentRepository.save(comment);
	    
	    CommentViewResponse response = commentMapper.toCommentViewResponse(savedComment, user.getId());
	    return response;
	}
	
	public CommentViewResponse deleteComment(String commentId, DeleteCommentRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = authentication.getName();
	    User user = userService.getUserByEmailWithoutStat(email)
	            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

	    Comment comment = commentRepository.findById(commentId)
	            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
	    
	    boolean isOwner = comment.getUserId().equals(user.getId());
	    boolean isAdmin = user.getRole().getName().equals("ADMIN");
	    
	    if (!isOwner && !isAdmin) {
	        throw new AppException(ErrorCode.UNAUTHORIZED);
	    }
	    
	    comment.setActive(false);
	    comment.setDeleted(true);
	    comment.setDeleteReasonTag(request.getReasonTag());
	    comment.setDeleteReason(request.getReason());
	    
	    Comment deletedComment = commentRepository.save(comment);
	    
	    CommentViewResponse response = commentMapper.toCommentViewResponse(deletedComment, user.getId());
	    
	    return response;
	}
	
	public Comment getComment(String commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new AppException(ErrorCode.COMMENT_REPORT_NOT_FOUND));
		return comment;
	}
	
	


}
