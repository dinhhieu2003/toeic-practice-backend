package com.toeic.toeic_practice_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.toeic.toeic_practice_backend.domain.entity.Question;

public interface QuestionRepository extends MongoRepository<Question, String> {
	@Query(value = "{ 'testId': ?0, 'type': { $ne: 'subquestion' }, 'partNum': { $in: ?1 } }", 
		       fields = "{ 'id': 1, 'questionNum': 1, 'partNum': 1, 'type': 1, 'subQuestions': 1, 'content': 1, 'resources': 1,'answers': 1 }")
	List<Question> findByTestIdAndTypeIsNotSubquestion(String testId, List<Integer> listPart);
	List<Question> findByIdIn(List<String> listQuestionIds);
	@Query(value = "{ '_id': { $in: ?0 } }", 
	       fields = "{ 'id': 1, 'testId': 1, 'practiceId': 1, 'parentId': 1, 'questionNum': 1, 'partNum': 1, 'type': 1, 'content': 1, 'difficulty': 1, 'topic._id': 1, 'resources': 1, 'transcript': 1, 'explanation': 1, 'answers': 1, 'correctAnswer': 1 }")
	List<Question> findByIdInWithTopicIds(List<String> listQuestionIds);
	@Query("{ 'testId': ?0, 'type': { '$in': ['single', 'group'] } }")
	Page<Question> findByTestId(String testId, Pageable pageable);
	List<Question> findByTestId(String testId);
}
