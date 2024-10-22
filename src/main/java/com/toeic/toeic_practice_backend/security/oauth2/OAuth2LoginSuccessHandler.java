package com.toeic.toeic_practice_backend.security.oauth2;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.toeic.toeic_practice_backend.domain.entity.Role;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.service.RoleService;
import com.toeic.toeic_practice_backend.service.UserService;
import com.toeic.toeic_practice_backend.utils.security.JwtTokenUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService; 
    private final RoleService roleService;
    private final JwtTokenUtils jwtTokenUtils;
    private final Encoder encoder;
    @Value("${client.url}")
    private String clientUrl;
    private User user = null;
    private String message = "Đăng nhập thành công";
    private Integer status = HttpStatus.OK.value();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
    	
        String targetUrl = this.clientUrl + "/oauth2/redirect";

        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();
        String email = attributes.getOrDefault("email", "").toString();
        String avatar = attributes.getOrDefault("picture", "").toString();
        log.info("Login account: " + email);

        this.userService.getUserByEmail(email).ifPresentOrElse(user -> {
            if (!user.isActive()) {
                this.message = "Tài khoản của bạn đang bị khóa";
                this.status = HttpStatus.FORBIDDEN.value();
            } else {
                this.user = user;
            }
        }, () -> {
            User user = new User();
            user.setEmail(email);
            user.setAvatar(avatar);
            user.setActive(true);
            Role role = roleService.getRoleByName("USER");
            user.setRole(role);
            this.user = this.userService.saveUser(user);
        });
        if (this.user != null) {
            String accessToken = this.jwtTokenUtils.createAccessToken(user);
            String refreshToken = this.jwtTokenUtils.createRefreshToken(user);
         // Set refresh token to cookie
            ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(jwtTokenUtils.getRefreshTokenExpiration())
                    .build();
            this.user.setRefreshToken(refreshToken);
            this.user = this.userService.saveUser(user);
            response.addHeader("Set-Cookie", responseCookie.toString());
         // Save the user's authentication into SecurityContextHolder
	            Authentication newAuth = new UsernamePasswordAuthenticationToken(
	                    this.user.getEmail(), null, authentication.getAuthorities());
	            System.out.println("success: "+ newAuth);
	            SecurityContextHolder.getContext().setAuthentication(newAuth);
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("status", this.status)
                    .queryParam("iduser", this.user.getId())
                    .queryParam("token", accessToken)
                    .queryParam("email", this.encoder.encode(this.user.getEmail()))
                    .queryParam("avatar", this.encoder.encode(this.user.getAvatar()))
                    .queryParam("role", this.user.getRole().getName())
                    .build().toUriString();
            log.info("Login account success: " + email);
        } else {
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("status", this.status)
                    .queryParam("message", this.message)
                    .build().toUriString();
            log.error("Login account failed: " + email);
        }
        response.sendRedirect(targetUrl);
    }
}