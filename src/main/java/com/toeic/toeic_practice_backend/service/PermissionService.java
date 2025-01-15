package com.toeic.toeic_practice_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.permission.CreatePermissionRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.permission.UpdatePermissionStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.PermissionRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
	private final PermissionRepository permissionRepository;
	private final MongoTemplate mongoTemplate;
	
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
	
	public Permission updatePermissionStatus(UpdatePermissionStatusRequest updatePermissionStatus, String permissionId) {
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
	
	public PaginationResponse<List<Permission>> getAllPermission(Pageable pageable, Boolean active, String search) {
		Query query = new Query();
		if (active != null) {
	        query.addCriteria(Criteria.where("isActive").is(active));
	    }
		
		if (search != null && !search.isEmpty()) {
	        query.addCriteria(Criteria.where("name").regex(search, "i"));
	    }
		
		// Áp dụng phân trang
	    query.with(pageable);

	    // Lấy danh sách kết quả
	    List<Permission> permissions = mongoTemplate.find(query, Permission.class);

	    // Tính tổng số phần tử cho phân trang
	    long totalItems = mongoTemplate.count(query.skip(0).limit(0), Permission.class);

	    // Trả về Page
	    Page<Permission> permissionPage = new PageImpl<>(permissions, pageable, totalItems);
		
	    PaginationResponse<List<Permission>> response = 
	    		PaginationUtils.buildPaginationResponse(pageable, permissionPage);

	    return response;
	}
}
