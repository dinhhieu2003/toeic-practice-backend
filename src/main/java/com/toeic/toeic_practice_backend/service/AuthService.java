package com.toeic.toeic_practice_backend.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.auth.Tokens;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.security.JwtTokenUtils;
import com.toeic.toeic_practice_backend.utils.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	private final JwtTokenUtils jwtTokenUtils;
	private final UserService userService;
	private final JwtDecoder jwtDecoder;
	private final RedisTemplate<String, String> redisTemplate;
	private final long EXTRA_MINUTES_ENSURE_INVALIDATION = 1;
	
	public void logout() {
		log.info("Start logout function");
		String token = SecurityUtils.getCurrentUserJWT()
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
		// Calculate remaining time of this token, that is also expiry in redis
		Jwt jwt = jwtDecoder.decode(token);
		Instant expiresAt = jwt.getExpiresAt();
		Instant currentTime = Instant.now();
		long remainingTimeMinutes = Math.max(0, 
	            Duration.between(currentTime, expiresAt).toMinutes() + EXTRA_MINUTES_ENSURE_INVALIDATION);
		// Put this token into blacklist
		redisTemplate.opsForValue().set(
                jwt.getId(), 
                "revoked", 
                Duration.ofMinutes(remainingTimeMinutes)
         );
	}
	
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
