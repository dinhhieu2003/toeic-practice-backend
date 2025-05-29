package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.TestDraft;

@Repository
public interface TestDraftRepository extends MongoRepository<TestDraft, String> {
	boolean existsByTestIdAndUserId(String testId, String userId);
	Optional<TestDraft> findByTestIdAndUserId(String testId, String userId);
}
