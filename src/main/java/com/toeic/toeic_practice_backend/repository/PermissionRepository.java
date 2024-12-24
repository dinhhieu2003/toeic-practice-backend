package com.toeic.toeic_practice_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Permission;

@Repository
public interface PermissionRepository extends MongoRepository<Permission, String> {
	Page<Permission> findAll(Pageable pageable);
}
