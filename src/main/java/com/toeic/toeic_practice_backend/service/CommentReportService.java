package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.commentReport.CreateCommentReportRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.commentReport.UpdateCommentReportRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Comment;
import com.toeic.toeic_practice_backend.domain.entity.CommentReport;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.CommentReportRepository;
import com.toeic.toeic_practice_backend.repository.specification.CommentReportSpecification;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportReasonCategory;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportStatus;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentReportService {
	private final MongoTemplate mongoTemplate;
	private final UserService userService;
	private final CommentService commentService;
	private final CommentReportRepository commentReportRepository;
	public PaginationResponse<List<CommentReport>> getCommentReports(Pageable pageable, String term, String[] sortBy, String[] sortDirection, Boolean active,
			CommentTargetType commentContextType, String commentContextId, 
			CommentReportStatus status, CommentReportReasonCategory reasonCategory) {
		if(sortBy == null || sortBy.length == 0) {
			sortBy = new String[] {"createdAt"};
		}
		
		if(sortDirection == null || sortDirection.length == 0) {
			sortDirection = new String[] {"desc"};
		}
		
		CommentReportSpecification spec = new CommentReportSpecification(term, sortBy, sortDirection, active, commentContextType, commentContextId, status, reasonCategory);
		Query query = spec.buildQuery(pageable);
		List<CommentReport> commentReports = mongoTemplate.find(query, CommentReport.class);
		long totalItems = mongoTemplate.count(query.skip(0).limit(0), CommentReport.class);
		Page<CommentReport> pageData = new PageImpl<>(commentReports, pageable, totalItems);
		PaginationResponse<List<CommentReport>> response =
				PaginationUtils.buildPaginationResponse(pageable, pageData);
		return response;
	}
	
	public CommentReport createCommentReport(CreateCommentReportRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userId = null;
        Optional<User> user = userService.getUserByEmailWithoutStat(username);
        if(user.isPresent()) {
        	userId = user.get().getId();
        }
        
        Comment comment = commentService.getComment(request.getReportedCommentId());
        
        CommentReport report = CommentReport.builder()
        		.reportedCommentId(request.getReportedCommentId())
        		.reporterUserId(userId)
        		.commentContextId(request.getCommentContextId())
        		.commentContextType(request.getCommentContextType())
        		.reasonCategory(request.getReasonCategory())
        		.reasonDetails(request.getReasonDetails())
        		.status(CommentReportStatus.PENDING_REVIEW)
        		.probInsult(comment.getProbInsult())
        		.probHateSpeech(comment.getProbHateSpeech())
        		.probObscene(comment.getProbObscene())
        		.probSevereToxicity(comment.getProbSevereToxicity())
        		.probSpam(comment.getProbSpam())
        		.probThreat(comment.getProbThreat())
        		.build();
        CommentReport response = commentReportRepository.save(report);
        return response;
	}
	
	public CommentReport updateReport(String reportId, UpdateCommentReportRequest request) {
		CommentReport report = commentReportRepository.findById(reportId)
				.orElseThrow(() -> new AppException(ErrorCode.COMMENT_REPORT_NOT_FOUND));
		if(report.getReviewedByAdminId() == null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        String userId = null;
	        Optional<User> user = userService.getUserByEmailWithoutStat(username);
	        if(user.isPresent()) {
	        	userId = user.get().getId();
	        }
	        report.setReviewedByAdminId(userId);
		}
		report.setAdminNotes(request.getAdminNotes());
		report.setStatus(request.getStatus());
		CommentReport response = commentReportRepository.save(report);
		return response;
	}
	
	public void deleteCommentReport(String reportId) {
		commentReportRepository.deleteById(reportId);
	}
}
