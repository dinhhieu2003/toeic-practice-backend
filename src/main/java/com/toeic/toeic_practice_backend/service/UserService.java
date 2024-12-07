package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserInfoResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateStatusResponse;
import com.toeic.toeic_practice_backend.domain.entity.Role;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.UserMapper;
import com.toeic.toeic_practice_backend.repository.RoleRepository;
import com.toeic.toeic_practice_backend.repository.UserRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	
	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public Optional<User> getUserByEmailWithOnlyRole(String email) {
		return userRepository.findByEmailWithOnlyRole(email);
	}
	
	public Optional<User> getUserByEmailWithoutStat(String email) {
		return userRepository.findByEmailWithoutStat(email);
	}
	
	public User saveUser(User user) {
		return userRepository.save(user);
	}
	
	public List<User> getAllUserInIds(List<String> listUserIds) {
		return userRepository.findAllById(listUserIds);
	}
	
	public void saveAllUsers(List<User> users) {
		userRepository.saveAll(users);
	}
	
	public UserUpdateRoleResponse updateUserRole(String id, String roleId) {
		User newUser = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
		Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
		newUser.setRole(role);
		newUser = userRepository.save(newUser);
		UserUpdateRoleResponse userUpdateRoleDto = userMapper.toUserUpdateRoleResponseFromUser(newUser);
		return userUpdateRoleDto;
	}
	
	public PaginationResponse<List<UserInfoResponse>> getAllUser(Pageable pageable) {
		Page<UserInfoResponse> userPage = userRepository.findAllUserInfo(pageable);
	    
		PaginationResponse<List<UserInfoResponse>> response = new PaginationResponse<List<UserInfoResponse>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(userPage.getTotalElements());
		meta.setTotalPages(userPage.getTotalPages());
		response.setMeta(meta);
		response.setResult(userPage.getContent());
		return response;
	}
	
	public UserUpdateStatusResponse updateUserStatus(String id, boolean isActive) {
		User newUser = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
		newUser.setActive(isActive);
		newUser = userRepository.save(newUser);
		UserUpdateStatusResponse userUpdateStatusDto = userMapper.toUserUpdateStatusResponseFromUser(newUser);
		return userUpdateStatusDto;
	}
	
	public Optional<User> findUserByEmailAndRefreshToken(String email, String refreshToken) {
		Optional<User> user = userRepository.findByEmailAndRefreshToken(email, refreshToken);
		return user;
	}
	
	public void updateRefreshToken(User user, String refreshToken) {
		user.setRefreshToken(refreshToken);
		saveUser(user);
	}
}
