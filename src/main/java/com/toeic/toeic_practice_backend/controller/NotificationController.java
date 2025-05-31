package com.toeic.toeic_practice_backend.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.response.notification.NotificationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.service.NotificationService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${api.prefix}/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
	private final NotificationService notificationService;
	
	@GetMapping("")
	public ResponseEntity<PaginationResponse<List<NotificationResponse>>> getNotifications(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize) {
		log.info("User is getting all his/her notifications");
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(notificationService.getNotifications(pageable));
	}
	
	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<NotificationResponse> markAsRead(
			@PathVariable String notificationId) {
		log.info("User mark notification id {} as read", notificationId);
		return ResponseEntity.ok(notificationService.markAsRead(notificationId));
	}
	
	@PatchMapping("/read-all")
	public ResponseEntity<Map<String, Long>> markAllAsRead() {
        long count = notificationService.markAllNotificationsAsRead();
        return ResponseEntity.ok(Collections.singletonMap("markedAsReadCount", count));
    }
	
	@DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
