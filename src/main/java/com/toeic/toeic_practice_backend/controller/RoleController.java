package com.toeic.toeic_practice_backend.controller;

import java.util.List;

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

import com.toeic.toeic_practice_backend.domain.dto.request.role.RoleCreationRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.role.UpdateRoleStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Role;
import com.toeic.toeic_practice_backend.service.RoleService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/roles")
public class RoleController {
	private final RoleService roleService;
	
	@PostMapping("")
	public ResponseEntity<Role> createRole(@RequestBody RoleCreationRequest role) {
		Role newRole = roleService.createRole(role);
		return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Role> updateRole(@RequestBody RoleCreationRequest role, @PathVariable String id) {
		Role updatedRole = roleService.updateRole(role, id);
		return ResponseEntity.ok(updatedRole);
	}
	
	@PutMapping("{roleId}/status")
	public ResponseEntity<Role> updateRoleStatus(@RequestBody UpdateRoleStatusRequest role, @PathVariable String roleId) {
		Role updatedRole = roleService.updateRoleStatus(role, roleId);
		return ResponseEntity.ok(updatedRole);
	}
	
	@GetMapping("")
	public ResponseEntity<PaginationResponse<List<Role>>> getAllRoles(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
	        @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false, defaultValue = "") String search) {
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(roleService.getAllRoles(search, pageable));
	}
}
