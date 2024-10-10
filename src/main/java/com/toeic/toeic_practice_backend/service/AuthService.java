package com.toeic.toeic_practice_backend.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.auth.LoginResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.auth.Tokens;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.utils.security.JwtTokenUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtTokenUtils jwtTokenUtils;
	private final UserService userService;
	
	public Tokens handleRefreshToken(User user) {
		String accessToken = jwtTokenUtils.createAccessToken(user);
		String refreshToken = jwtTokenUtils.createRefreshToken(user);
		userService.updateRefreshToken(user, refreshToken);
		return new Tokens(accessToken, refreshToken);
	}
}
