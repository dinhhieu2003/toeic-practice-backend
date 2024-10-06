package com.toeic.toeic_practice_backend.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final Encoder encoder;
    @Value("${client.url}")
    private String clientUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 authentication failed: {} ", exception.getMessage());
        String targetUrl = this.clientUrl + "/oauth2/redirect";

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("status", HttpStatus.UNAUTHORIZED.value())
                .queryParam("message", encoder.encode("Quá trình xác thực đã thất bại! Vui lòng thử lại!"))
                .build().toUriString();
        response.sendRedirect(targetUrl);
    }
}