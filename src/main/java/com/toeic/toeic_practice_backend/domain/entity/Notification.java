package com.toeic.toeic_practice_backend.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.toeic.toeic_practice_backend.utils.constants.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "notifications")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
	@Id
	private String id;
	private String userId;
	private NotificationType type;
	private String message;
	private String relatedId;
	private boolean isRead;
	private String deepLink;
}
