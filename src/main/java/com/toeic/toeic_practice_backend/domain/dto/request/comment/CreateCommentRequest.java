package com.toeic.toeic_practice_backend.domain.dto.request.comment;

import java.util.ArrayList;
import java.util.List;

import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
	private String content;

    private CommentTargetType targetType;
    private String targetId;

    private String parentId;	// null if root comment
    private List<String> mentionedUserIds = new ArrayList<>();
}
