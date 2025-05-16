package com.toeic.toeic_practice_backend.domain.dto.response.comment;

import java.time.Instant;
import java.util.HashSet;

import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.DeleteReasonTagComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentViewResponse {
	private String id;
	private String content;
	private String userId;
    private String userDisplayName;
    private String userAvatarUrl;
    private String parentId;
    private String rootId;
    private HashSet<String> mentionedUserIds = new HashSet<>();
    private CommentTargetType targetType;
    private String targetId;  
    private int likeCounts;   
    private int directReplyCount; 
    private int level;
    private boolean deleted;
    private DeleteReasonTagComment deleteReasonTag; 
    private String deleteReason;
    private boolean isLikedByCurrentUser;
    private Instant createdAt;
    private boolean active;
    private float probInsult;
    private float probThreat;
    private float probHateSpeech;
    private float probSpam;
    private float probSevereToxicity;
    private float probObscene;
}
