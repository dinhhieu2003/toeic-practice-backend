package com.toeic.toeic_practice_backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
	Optional<Role> findByName(String name);
}
