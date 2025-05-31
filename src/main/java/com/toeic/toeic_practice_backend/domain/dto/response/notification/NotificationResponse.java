package com.toeic.toeic_practice_backend.domain.dto.response.notification;

import java.time.Instant;

import com.toeic.toeic_practice_backend.utils.constants.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
	private String id;
	private NotificationType type;
	private String message;
	private String relatedId;
	private boolean isRead;
	private Instant createdAt;
	private String deepLink;
}
