package com.toeic.toeic_practice_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
	private final PermissionRepository permissionRepository;
	
	public Permission createPermission(Permission permission) {
		return permissionRepository.save(permission);
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
