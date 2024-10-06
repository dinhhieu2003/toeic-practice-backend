package com.toeic.toeic_practice_backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public User saveUser(User user) {
		return userRepository.save(user);
	}
}
