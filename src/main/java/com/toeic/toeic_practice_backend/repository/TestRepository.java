package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Test;

@Repository
public interface TestRepository extends MongoRepository<Test, String> {
	Optional<Test> findByNameAndCategory_Id(String name, String categoryId);
}
