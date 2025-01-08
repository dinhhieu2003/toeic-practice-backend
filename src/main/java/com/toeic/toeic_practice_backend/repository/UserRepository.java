package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.dto.response.user.UserInfoResponse;
import com.toeic.toeic_practice_backend.domain.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	@Query(value = "{ 'email': ?0 }", fields = "{ 'learningProgress': 1 }")
	public Optional<User> findByEmailWithOnlyLearningProgress(String email);
	@Query(value = "{ 'email': ?0 }", fields = "{ 'testHistory': 1 }")
	public Optional<User> findByEmailWithOnlyTestHistory(String email);
    public Optional<User> findByEmail(String email);
    public Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);
    @Query(value = "{ 'email': ?0 }", fields = "{ 'role': 1 }")
    public Optional<User> findByEmailWithOnlyRole(String email);
    @Query(value = "{ 'email': ?0 }", fields = "{ 'id': 1, 'email': 1, "
    		+ "'avatar': 1, 'refreshToken': 1, 'target': 1, 'role': 1 }")
    public Optional<User> findByEmailWithoutStat(String email);
    // findAllUserInfo => findAll user with id, email, role, target, isActive
    @Query(value = "{}", fields = "{'id': 1, 'email': 1, 'role': 1, 'target': 1 ,'isActive': 1}")
    Page<UserInfoResponse> findAllUserInfo(Pageable pageable);
    
    @Query(value = "{ 'email': {$regex: ?0, $options: 'i'} }", fields = "{'id': 1, 'email': 1, 'role': 1, 'target': 1 ,'isActive': 1}")
    Page<UserInfoResponse> findUserInfoByEmailContaining(String search, Pageable pageable);
}