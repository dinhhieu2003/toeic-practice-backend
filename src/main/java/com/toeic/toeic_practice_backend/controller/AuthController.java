package com.toeic.toeic_practice_backend.controller;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.response.auth.LoginResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.auth.Tokens;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.service.AuthService;
import com.toeic.toeic_practice_backend.service.UserService;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.security.JwtTokenUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final JwtTokenUtils jwtTokenUtils;
	private final AuthService authService;
	
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
	
	@GetMapping("/refresh")
    public ResponseEntity<LoginResponse> handleRefreshToken(
            @CookieValue(name = "refresh_token") String refresh_token) {

        Jwt decodedOldRefreshToken = this.jwtTokenUtils.checkValidRefreshToken(refresh_token);

        String email = decodedOldRefreshToken.getSubject();

        Optional<User> optionalUser = userService.findUserByEmailAndRefreshToken(email, refresh_token);

        if (!optionalUser.isPresent()) {
            throw new AppException(ErrorCode.TOKEN_NOT_VALID);
        }
        
        User user = optionalUser.get();
        // handle refresh token
        Tokens tokens = authService.handleRefreshToken(user);
        System.out.println(tokens);
        
     // Set refresh token to cookie
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenUtils.getRefreshTokenExpiration())
                .build();
        LoginResponse response = LoginResponse.builder()
        		.id(user.getId())
        		.email(user.getEmail())
        		.avatar(user.getAvatar())
        		.accessToken(tokens.getAccessToken())
        		.build();
        return ResponseEntity.ok()
        		.header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        		.body(response);

    }
}