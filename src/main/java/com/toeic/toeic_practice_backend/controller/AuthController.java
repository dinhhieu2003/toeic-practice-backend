package com.toeic.toeic_practice_backend.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	
	@GetMapping("/account")
	public ResponseEntity<User> getCurrentAccount() {
		// Lấy thông tin Authentication hiện tại từ SecurityContextHolder
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    
	    // Kiểm tra xem authentication có null hoặc chưa xác thực hay không
	    if (authentication == null || !authentication.isAuthenticated()) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    
	    // Lấy email từ Authentication
	    String email = authentication.getName();
	    System.out.println("Email: " + email);
	    System.out.println(authentication);
	    // Tìm kiếm người dùng theo email
	    Optional<User> userOptional = userService.getUserByEmail(email);
	    if (userOptional.isPresent()) {
	        User user = userOptional.get();
	        return ResponseEntity.ok(user);
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }
	}
	
	@GetMapping("/hello")
	public String hello() {
		System.out.println("Hello");
		return "Hello";
	}
}