package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Topic;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
	Optional<Topic> findByName(String name);
}