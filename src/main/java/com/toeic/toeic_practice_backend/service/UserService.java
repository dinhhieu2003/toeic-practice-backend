package com.toeic.toeic_practice_backend.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.user.UpdateUserTargetRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UpdateUserTargetResponse;
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
	
	public HashMap<String, Integer> getUserLearningProgress(String email) {
		User user = userRepository.findByEmailWithOnlyLearningProgress(email)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		HashMap<String, Integer> response = user.getLearningProgress();
		return response;
	}
	
	public HashSet<String> getUserTestHistory(String email) {
		User user = userRepository.findByEmailWithOnlyTestHistory(email)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		return user.getTestHistory();
	}
	
	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public Optional<User> getUserByEmailWithOnlyRole(String email) {
		return userRepository.findByEmailWithOnlyRole(email);
	}
	
	public Optional<User> getUserByEmailWithoutStat(String email) {
		return userRepository.findByEmailWithoutStat(email);
	}
	
	public User getUserById(String id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		return user;
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
	
	public PaginationResponse<List<UserInfoResponse>> getAllUser(String search, Pageable pageable) {
		Page<UserInfoResponse> userPage = null;
		if(search.isEmpty()) {
			userPage = userRepository.findAllUserInfo(pageable);
		} else if(!search.isEmpty()) {
			userPage = userRepository.findUserInfoByEmailContaining(search, pageable);
		}
	    
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
	
	public UpdateUserTargetResponse updateUserTarget(String userId ,UpdateUserTargetRequest updateUserTargetRequest) {
		User currentUser = getUserById(userId);
		currentUser.setTarget(updateUserTargetRequest.getTarget());
		User newUser = userRepository.save(currentUser);
		UpdateUserTargetResponse updateUserTargetResponse = new UpdateUserTargetResponse();
		updateUserTargetResponse.setEmail(newUser.getEmail());
		updateUserTargetResponse.setTarget(newUser.getTarget());
		return updateUserTargetResponse;
	}
}
