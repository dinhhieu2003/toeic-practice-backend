package com.toeic.toeic_practice_backend.domain.dto.request.commentReport;

import com.toeic.toeic_practice_backend.utils.constants.CommentReportReasonCategory;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentReportRequest {
	private String reportedCommentId;
	private CommentReportReasonCategory reasonCategory;
	private String reasonDetails;
	private CommentTargetType commentContextType;
	private String commentContextId;
}
