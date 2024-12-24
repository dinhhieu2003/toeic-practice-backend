package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.permission.CreatePermissionRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.permission.UpdatePermissionStatus;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.service.PermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
	private final PermissionService permissionService;
	
	@PostMapping("")
	public ResponseEntity<Permission> createPermission(@RequestBody CreatePermissionRequest permission) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(permissionService.createPermission(permission));
	}
	
	@PutMapping("{permissionId}")
	public ResponseEntity<Permission> updatePermission(@RequestBody Permission permission, @PathVariable String permissionId) {
		return ResponseEntity.ok(permissionService.updatePermission(permission, permissionId));
	}
	
	@PutMapping("{permissionId}/status")
	public ResponseEntity<Permission> updatePermissionStatus(@RequestBody UpdatePermissionStatus permissionStatus, @PathVariable String permissionId ) {
		return ResponseEntity.ok(permissionService.updatePermissionStatus(permissionStatus, permissionId));
	}
	
	@GetMapping
	public ResponseEntity<PaginationResponse<List<Permission>>> getAllPermission(
			@RequestParam(defaultValue = "1") String current,
			@RequestParam(defaultValue = "5") String pageSize, 
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false, defaultValue = "") String search) {
		int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
		System.out.println(active);
		return ResponseEntity.ok(permissionService.getAllPermission(pageable, active, search));
	}
	
	
}
