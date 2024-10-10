package com.toeic.toeic_practice_backend.service;

import org.springframework.stereotype.Service;

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
}
