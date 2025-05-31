package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.TestDraft;
import com.toeic.toeic_practice_backend.repository.projectionInterface.TestDraftVersionOnly;

@Repository
public interface TestDraftRepository extends MongoRepository<TestDraft, String> {
	boolean existsByTestIdAndUserId(String testId, String userId);
	Optional<TestDraft> findByTestIdAndUserId(String testId, String userId);
	
	@Query(value = "{ 'testId': ?0, 'userId': ?1 }", fields = "{ 'version': 1, '_id': 0 }")
	Optional<TestDraftVersionOnly> findVersionByTestIdAndUserId(String testId, String userId);
}
