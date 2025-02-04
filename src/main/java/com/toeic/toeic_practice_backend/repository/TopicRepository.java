package com.toeic.toeic_practice_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Topic;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
	Optional<Topic> findByName(String name);
	List<Topic> findByIdIn(List<String> listTopicIds);
	Page<Topic> findAll(Pageable pageable);
	@Query("{ 'name': {$regex: ?0, $options: 'i'} }")
	Page<Topic> findByNameContaining(String search, Pageable pageable);
}