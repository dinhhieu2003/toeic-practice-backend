package com.toeic.toeic_practice_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
	Optional<Category> findByFormatAndYear(String format, int year);
	List<Category> findByFormatAndIsActiveTrue(String format);
	List<Category> findByIsActiveTrue();
	@Query("{'format': {$regex: ?0, $options: 'i'}}")
	Page<Category> findByFormatContaining(String search, Pageable pageable);
}