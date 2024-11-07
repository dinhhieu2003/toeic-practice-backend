package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
	Optional<Category> findByFormatAndYear(String format, int year);
}