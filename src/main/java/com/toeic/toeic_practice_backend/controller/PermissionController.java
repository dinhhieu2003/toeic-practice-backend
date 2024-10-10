package com.toeic.toeic_practice_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.service.PermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
	private final PermissionService permissionService;
	
	@PostMapping("")
	public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(permissionService.createPermission(permission));
	}
}
