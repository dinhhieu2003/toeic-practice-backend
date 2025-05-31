package com.toeic.toeic_practice_backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.toeic.toeic_practice_backend.domain.dto.response.notification.NotificationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
	
	NotificationResponse notificationToNotificationResponse(Notification notification);
	
	default Page<NotificationResponse> pageNotificationToPageNotificationResponse(Page<Notification> pageNotification) {
		List<NotificationResponse> content = pageNotification.getContent()
				.stream()
				.map(this::notificationToNotificationResponse)
				.collect(Collectors.toList());
		return new PageImpl<>(content, pageNotification.getPageable(), pageNotification.getTotalElements());
	}
}
