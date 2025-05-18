package com.toeic.toeic_practice_backend.domain.dto.request.commentReport;

import com.toeic.toeic_practice_backend.utils.constants.CommentReportStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentReportRequest {
	private CommentReportStatus status;
	private String adminNotes;
}
