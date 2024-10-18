package com.toeic.toeic_practice_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.toeic.toeic_practice_backend.domain.entity.Question;

public interface QuestionRepository extends MongoRepository<Question, String> {

}
