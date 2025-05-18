package com.toeic.toeic_practice_backend.domain.entity;

import java.util.HashSet;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.toeic.toeic_practice_backend.utils.constants.CommentReportReasonCategory;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportStatus;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.DeleteReasonTagComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "comment_reports")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReport extends BaseEntity {
	@Id
	private String id;
	private String reportedCommentId;
	private String reporterUserId;
	private CommentTargetType commentContextType;
	private String commentContextId;
	private CommentReportReasonCategory reasonCategory;
	private String reasonDetails;
	private CommentReportStatus status;
	private String reviewedByAdminId;
	private String adminNotes;
	private float probInsult;
    private float probThreat;
    private float probHateSpeech;
    private float probSpam;
    private float probSevereToxicity;
    private float probObscene;
}
