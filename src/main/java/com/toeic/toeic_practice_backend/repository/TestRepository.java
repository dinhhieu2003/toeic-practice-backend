package com.toeic.toeic_practice_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Test;

@Repository
public interface TestRepository extends MongoRepository<Test, String> {
	Optional<Test> findByNameAndCategory_Id(String name, String categoryId);
	Optional<Test> findByName(String name);
	Page<Test> findByCategory_Id(String categoryId, Pageable pageable);
	@Query("{ $and: [ " +
	           " { 'category._id': ?1 }, " + 
	           " { 'name': { $regex: ?0, $options: 'i' } } " +
	           "] }")
	Page<Test> findByTestNameContaining(String search, String categoryId, Pageable pageable);
	Page<Test> findByCategory_IdIn(List<String> listCategoryId, Pageable pageable);
	@Query("{ 'category.format': ?0, 'category.year': ?1, 'isActive': true }")
    Page<Test> findByFormatAndYear(String format, int year, Pageable pageable);

    @Query("{ 'category.format': ?0, 'isActive': true }")
    Page<Test> findByFormatOnly(String format, Pageable pageable);
    
    List<Test> findTestByIdIn(List<String> testIds);
}
