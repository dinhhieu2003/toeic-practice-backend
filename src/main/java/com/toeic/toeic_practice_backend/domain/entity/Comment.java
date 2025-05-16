package com.toeic.toeic_practice_backend.domain.entity;

import java.util.HashSet;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.DeleteReasonTagComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "comments")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "idx_targetType_targetId_parentId", def = "{'targetType': 1, 'targetId': 1, 'parentId': 1}")
public class Comment extends BaseEntity {
    @Id
    private String id;
    private String content;

    private String userId;
    private String userDisplayName;
    private String userAvatarUrl;
    
    private HashSet<String> likedUserIds = new HashSet<>();

    private String parentId;

    private String rootId;

    private HashSet<String> mentionedUserIds = new HashSet<>();
    // TargetType: TEST or LECTURE
    private CommentTargetType targetType;
    private String targetId;
    
    private int likeCounts;
    
    private int directReplyCount; 

    private int level;

    private boolean deleted;
    
 // Enum: VIOLATE_COMMUNITY_STANDARDS or USER_DELETE or ADMIN_DELETE
    private DeleteReasonTagComment deleteReasonTag; 

    private String deleteReason;
    
    private float probInsult;
    private float probThreat;
    private float probHateSpeech;
    private float probSpam;
    private float probSevereToxicity;
    private float probObscene;
}