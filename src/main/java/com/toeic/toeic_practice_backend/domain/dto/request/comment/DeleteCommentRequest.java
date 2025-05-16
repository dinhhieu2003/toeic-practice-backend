package com.toeic.toeic_practice_backend.domain.dto.request.comment;

import com.toeic.toeic_practice_backend.utils.constants.DeleteReasonTagComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteCommentRequest {
	private DeleteReasonTagComment reasonTag;
	private String reason;
}
