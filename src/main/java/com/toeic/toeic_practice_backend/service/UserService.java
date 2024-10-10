package com.toeic.toeic_practice_backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
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
	
	public User saveUser(User user) {
		return userRepository.save(user);
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
	
	public Optional<User> findUserByEmailAndRefreshToken(String email, String refreshToken) {
		Optional<User> user = userRepository.findByEmailAndRefreshToken(email, refreshToken);
		return user;
	}
	
	public void updateRefreshToken(User user, String refreshToken) {
		user.setRefreshToken(refreshToken);
		saveUser(user);
	}
}
