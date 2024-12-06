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
    public Optional<User> findByEmail(String email);
    public Optional<User> findByEmailAndRefreshToken(String email, String refreshToken);
    @Query(value = "{ 'email': ?0 }", fields = "{ 'id': 1, 'email': 1, 'avatar': 1, 'role': 1, 'target': 1, 'refreshToken': 1, 'isActive': 1 }")
    public Optional<User> findByEmailWithoutStat(String email);
    // findAllUserInfo => findAll user with id, email, role, target, isActive
    @Query(value = "{}", fields = "{'id': 1, 'email': 1, 'role': 1, 'target': 1 ,'isActive': 1}")
    Page<UserInfoResponse> findAllUserInfo(Pageable pageable);
}