package com.toeic.toeic_practice_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.repository.projectionInterface.LectureNameOnly;

@Repository
public interface LectureRepository extends MongoRepository<Lecture, String> {
	Page<Lecture> findByIsActiveTrue(Pageable pageable);
	@Aggregation(pipeline = {
	        "{ $match: { _id: { $ne: ?0 }, isActive: true } }",
	        "{ $sample: { size: ?1 } }"
	    })
	List<Lecture> findRandomLecturesExcludingId(String lectureId, int size);
	List<Lecture> findByIdIn(List<String> ids);
	
	/**
	 * Find all active lectures for recommendation
	 */
	List<Lecture> findByIsActiveTrue();
	
	List<Lecture> findLectureByIdIn(List<String> lectureIds);
	
	@Query(value = "{ '_id': ?0 }", fields = "{ 'name': 1, '_id': 0 }")
	Optional<LectureNameOnly> findLectureNameByLectureId(String lectureId);
}