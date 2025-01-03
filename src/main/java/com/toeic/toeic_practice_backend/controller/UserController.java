package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.user.UserUpdateRoleRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.user.UserUpdateStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserInfoResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateStatusResponse;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.service.UserService;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
	private final UserService userService;
	
	@PutMapping("/{id}/role")
	public ResponseEntity<UserUpdateRoleResponse> updateUserRole(@PathVariable String id, @RequestBody UserUpdateRoleRequest role) {
		UserUpdateRoleResponse userUpdated = userService.updateUserRole(id, role.getRoleId());
		return ResponseEntity.ok(userUpdated);
	}
	
	@PutMapping("/{id}/status")
	public ResponseEntity<UserUpdateStatusResponse> updateUserStatus(@PathVariable String id, @RequestBody UserUpdateStatusRequest userUpdateStatusRequest) {
		UserUpdateStatusResponse userUpdated = userService.updateUserStatus(id,
				userUpdateStatusRequest.isActive());
		return ResponseEntity.ok(userUpdated);
	}
	
	@GetMapping
	public ResponseEntity<PaginationResponse<List<UserInfoResponse>>> getAllUser(
			@RequestParam(defaultValue = "1") String current,
			@RequestParam(defaultValue = "5") String pageSize,
			@RequestParam(required = false ,defaultValue = "") String search) {
		int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
		return ResponseEntity.ok(userService.getAllUser(search, pageable));
	}
}
