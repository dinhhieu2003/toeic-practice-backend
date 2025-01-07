package com.toeic.toeic_practice_backend.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
	
	public String getCurrentEmail() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = null;
	    if (authentication != null && authentication.isAuthenticated() 
	            && !(authentication instanceof AnonymousAuthenticationToken)) {
	    	email = authentication.getName();
	    }
	    return email;
	}
}
