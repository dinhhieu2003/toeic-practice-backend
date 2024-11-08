package com.toeic.toeic_practice_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.user.UserUpdateRoleRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
import com.toeic.toeic_practice_backend.service.UserService;

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
}
