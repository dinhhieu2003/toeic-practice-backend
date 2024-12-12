package com.toeic.toeic_practice_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.permission.CreatePermissionRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.permission.UpdatePermissionStatus;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.PermissionRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
	private final PermissionRepository permissionRepository;
	
	public Permission updatePermission(Permission permission, String permissionId) {
		Permission existingPermission = permissionRepository.findById(permissionId)
				.orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
		existingPermission.setApiPath(permission.getApiPath());
		existingPermission.setMethod(permission.getMethod());
		existingPermission.setModule(permission.getModule());
		existingPermission.setName(permission.getName());
		Permission newPermission = permissionRepository.save(existingPermission);
		return newPermission;
	}
	
	public Permission updatePermissionStatus(UpdatePermissionStatus updatePermissionStatus, String permissionId) {
		Permission existingPermission = permissionRepository.findById(permissionId)
				.orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
		existingPermission.setActive(updatePermissionStatus.isActive());
		Permission newPermission = permissionRepository.save(existingPermission);
		return newPermission;
	}
	
	public Permission createPermission(CreatePermissionRequest permission) {
		Permission newPermission = new Permission();
		newPermission.setApiPath(permission.getApiPath());
		newPermission.setMethod(permission.getMethod());
		newPermission.setModule(permission.getModule());
		newPermission.setName(permission.getName());
		return permissionRepository.save(newPermission);
	}
	
	public PaginationResponse<List<Permission>> getAllPermission(Pageable pageable, Boolean active) {
		Page<Permission> permissionPage;

	    if (active != null) {
	        // if not null, find by isActive
	        permissionPage = permissionRepository.findByIsActive(active, pageable);
	    } else {
	        // if null, find all
	        permissionPage = permissionRepository.findAll(pageable);
	    }

	    PaginationResponse<List<Permission>> response = new PaginationResponse<>();
	    Meta meta = new Meta();
	    meta.setCurrent(pageable.getPageNumber() + 1);
	    meta.setPageSize(pageable.getPageSize());
	    meta.setTotalItems(permissionPage.getTotalElements());
	    meta.setTotalPages(permissionPage.getTotalPages());
	    List<Permission> result = permissionPage.getContent();
	    response.setMeta(meta);
	    response.setResult(result);

	    return response;
	}
}
