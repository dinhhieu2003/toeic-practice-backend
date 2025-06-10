package com.toeic.toeic_practice_backend.security.oauth2;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String targetUrl = this.clientUrl + "/oauth2/redirect";
        Integer status = HttpStatus.OK.value();
        String message = "Đăng nhập thành công";
        User user = null;

        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();
        String email = attributes.getOrDefault("email", "").toString();
        String avatar = attributes.getOrDefault("picture", "").toString();
        log.info("Login account: " + email);

        Optional<User> optionalUser = this.userService.getUserByEmail(email);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            if (!existingUser.isActive()) {
                log.warn("Account is blocked: {}", email);
                message = "BLOCKED ACCOUNT";
                status = HttpStatus.FORBIDDEN.value();
            } else {
                user = existingUser;
            }
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setAvatar(avatar);
            newUser.setActive(true);
            newUser.setRole(roleService.getRoleByName("USER"));
            user = userService.saveUser(newUser);
        }

        if (user != null) {
            String accessToken = this.jwtTokenUtils.createAccessToken(user);
            String refreshToken = this.jwtTokenUtils.createRefreshToken(user);
            user.setRefreshToken(refreshToken);
            userService.saveUser(user);
            ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(jwtTokenUtils.getRefreshTokenExpiration())
                    .build();
            response.addHeader("Set-Cookie", responseCookie.toString());

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("status", status)
                    .queryParam("iduser", user.getId())
                    .queryParam("email", encoder.encode(user.getEmail()))
                    .queryParam("avatar", encoder.encode(user.getAvatar()))
                    .queryParam("role", user.getRole().getName())
                    .queryParam("token", accessToken)
                    .build().toUriString();
        } else {
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("status", status)
                    .queryParam("message", message)
                    .build().toUriString();
        }

        response.sendRedirect(targetUrl);
    }

}