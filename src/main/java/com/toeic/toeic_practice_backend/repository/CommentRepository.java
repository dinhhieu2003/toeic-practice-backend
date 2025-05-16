package com.toeic.toeic_practice_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Comment;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

}
