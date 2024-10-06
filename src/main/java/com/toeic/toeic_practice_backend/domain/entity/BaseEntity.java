package com.toeic.toeic_practice_backend.domain.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;

@Data
public class BaseEntity {
	private boolean isActive;
	@CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt; 
}
