package com.toeic.toeic_practice_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.toeic.toeic_practice_backend.domain.entity.Question;

public interface QuestionRepository extends MongoRepository<Question, String> {
	@Query(" { 'testId': ?0, 'type': { $in: [ 'single', 'group' ] }, 'partNum': { $in: ?1 }  } ")
	List<Question> findByTestIdAndTypeIsNotSubquestion(String testId, List<Integer> listPart);
	List<Question> findByIdIn(List<String> listQuestionIds);
	Page<Question> findByTestId(String testId, Pageable pageable);
}
