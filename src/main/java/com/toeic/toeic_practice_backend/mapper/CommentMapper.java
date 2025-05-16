package com.toeic.toeic_practice_backend.mapper;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.toeic.toeic_practice_backend.domain.dto.response.comment.CommentViewResponse;
import com.toeic.toeic_practice_backend.domain.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

	@Mapping(target = "likedByCurrentUser", ignore = true)
    CommentViewResponse toCommentViewResponse(Comment comment, @Context String currentUserId);

    @Mapping(target = "likedByCurrentUser", ignore = true)
    List<CommentViewResponse> toCommentViewResponseList(List<Comment> comments, @Context String currentUserId);

    @AfterMapping
    default void setIsLikedByCurrentUser(Comment comment,
                                         @MappingTarget CommentViewResponse response,
                                         @Context String currentUserId) {
        if (currentUserId != null && comment.getLikedUserIds() != null) {
            response.setLikedByCurrentUser(comment.getLikedUserIds().contains(currentUserId));
        } else {
            response.setLikedByCurrentUser(false);
        }
    }
}
