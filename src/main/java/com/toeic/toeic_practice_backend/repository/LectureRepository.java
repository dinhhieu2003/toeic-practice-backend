package com.toeic.toeic_practice_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Lecture;

@Repository
public interface LectureRepository extends MongoRepository<Lecture, String> {
	Page<Lecture> findByIsActiveTrue(Pageable pageable);
	@Aggregation(pipeline = {
	        "{ $match: { lectureId: { $ne: ?0 } } }",
	        "{ $sample: { size: ?1 } }"
	    })
	List<Lecture> findRandomLecturesExcludingId(String lectureId, int size);
	List<Lecture> findByIdIn(List<String> ids);
}