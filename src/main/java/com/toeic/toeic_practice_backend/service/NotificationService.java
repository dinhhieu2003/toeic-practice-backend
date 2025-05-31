package com.toeic.toeic_practice_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.notification.NotificationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Notification;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.NotificationMapper;
import com.toeic.toeic_practice_backend.repository.NotificationRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final UserService userService;
	private final NotificationMapper notificationMapper;
	
	public Notification createNotification(Notification notification) {
		return notificationRepository.save(notification);
	}
	
	public PaginationResponse<List<NotificationResponse>> getNotifications(Pageable pageable) {
		String userId = userService.getCurrentUserId();
		if(userId == null) {
			throw new AppException(ErrorCode.UNAUTHENTICATED);
		}
		Page<Notification> pageNotification = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
		Page<NotificationResponse> pageData = notificationMapper.pageNotificationToPageNotificationResponse(pageNotification);
		
		PaginationResponse<List<NotificationResponse>> response =
				PaginationUtils.buildPaginationResponse(pageable, pageData);
		return response;
	}
	
	public NotificationResponse markAsRead(String notificationId) {
		String userId = userService.getCurrentUserId();
		if(userId == null) {
			throw new AppException(ErrorCode.UNAUTHENTICATED);
		}
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
		if(notification.isRead()) {
			log.info("Notification id {} is read", notificationId);
		} else {
			notification.setRead(true);
			notification = notificationRepository.save(notification);
			log.info("Successfully marked notification {} as read for user {}", notificationId, userId);
		}
		return notificationMapper.notificationToNotificationResponse(notification);
	}
	
	public long markAllNotificationsAsRead() {
		String userId = userService.getCurrentUserId();
		if(userId == null) {
			throw new AppException(ErrorCode.UNAUTHENTICATED);
		}
        log.info("Attempting to mark all unread notifications as read for user {}", userId);
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);

        if (unreadNotifications.isEmpty()) {
            log.info("No unread notifications found for user {}.", userId);
            return 0;
        }

        unreadNotifications.forEach(notification -> notification.setRead(true));
        
        notificationRepository.saveAll(unreadNotifications);
        
        long count = unreadNotifications.size();
        log.info("Successfully marked {} notifications as read for user {}", count, userId);
        return count;
    }
	
	public void deleteNotification(String notificationId) {
		String userId = userService.getCurrentUserId();
		if(userId == null) {
			throw new AppException(ErrorCode.UNAUTHENTICATED);
		}
        log.info("Attempting to delete notification {} for user {}", notificationId, userId);
        
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
		
        notificationRepository.delete(notification);
        log.info("Successfully deleted notification {} for user {}", notificationId, userId);
    }
}
