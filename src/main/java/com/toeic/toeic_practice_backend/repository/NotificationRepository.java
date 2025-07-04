package com.toeic.toeic_practice_backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.toeic.toeic_practice_backend.domain.entity.Notification;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
	Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
	List<Notification> findByUserIdAndIsReadFalse(String userId);
}
