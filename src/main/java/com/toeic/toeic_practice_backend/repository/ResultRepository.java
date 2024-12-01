package com.toeic.toeic_practice_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Result;

@Repository
public interface ResultRepository extends MongoRepository<Result, String> {
    List<Result> findByUserId(String userId);

    @Query(value = "{ 'userId': ?0 }", fields = "{ 'userAnswers' : 0 }")
    Page<Result> findWithoutUserAnswersByUserId(String userId, Pageable pageable);

    @Query(value = "{ 'userId': ?0, 'testId': { $ne: '' }, 'type': 'practice' }", fields = "{ 'userAnswers' : 0 }")
    Page<Result> findWithoutUserAnswersByUserIdAndTypePractice(String userId, Pageable pageable);

    @Query(value = "{ 'userId': ?0, 'testId': { $ne: '' }, 'type': 'fulltest' }", fields = "{ 'userAnswers' : 0 }")
    Page<Result> findWithoutUserAnswersByUserIdAndTypeFullTest(String userId, Pageable pageable);

    @Query(value = "{ 'userId': ?0, 'testId': '' }")
    Page<Result> findByUserIdAndTestIdEmpty(String userId, Pageable pageable);
    
    List<Result> findByTestId(String testId);
    
    List<Result> findByUserIdAndTestIdIn(String userId, List<String> testIds);
    List<Result> findByUserIdAndTestId(String userId, String testId);
}
