package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.role.RoleCreationRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.domain.entity.Role;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.PermissionRepository;
import com.toeic.toeic_practice_backend.repository.RoleRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	
	public Role createRole(RoleCreationRequest role) {
		List<String> permissionIds = role.getPermissionIds();
		List<Permission> permissions = new ArrayList<>();
		if(permissionIds.size() > 0) {
			permissions = permissionRepository.findAllById(permissionIds);
		}
		
		Role newRole = new Role();
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		newRole.setPermissions(permissions);
		newRole = roleRepository.save(newRole);
		return newRole;
	}
	
	public Role updateRole(RoleCreationRequest roleRequest, String id) {
		Role role = roleRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
		List<String> permissionIds = roleRequest.getPermissionIds();
		List<Permission> permissions = new ArrayList<>();
		if(permissionIds.size() > 0) {
			permissions = permissionRepository.findAllById(permissionIds);
		}
		role.setName(roleRequest.getName());
		role.setDescription(roleRequest.getDescription());
		role.setPermissions(permissions);
		Role updatedRole = roleRepository.save(role);
		return updatedRole;
	}
	
	public PaginationResponse<List<Role>> getAllRoles(Pageable pageable) {
		Page<Role> pageRoles = roleRepository.findAll(pageable);
		List<Role> listRoles = pageRoles.getContent();
		PaginationResponse<List<Role>> response = new PaginationResponse<>();
		Meta meta = Meta.builder().current(pageRoles.getNumber())
				.pageSize(pageRoles.getSize()).totalItems(pageRoles.getTotalElements())
				.totalPages(pageRoles.getTotalPages()).build();
		response.setMeta(meta);
		response.setResult(listRoles);
		return response;
	}
}
