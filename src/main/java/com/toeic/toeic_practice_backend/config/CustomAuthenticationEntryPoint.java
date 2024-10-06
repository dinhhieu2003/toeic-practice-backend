package com.toeic.toeic_practice_backend.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.toeic_practice_backend.domain.dto.response.ApiResponse;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        this.delegate.commence(request, response, authException);

        response.setContentType("application/json;charset=UTF-8");



        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        String errorMessage = errorCode.getMessage();
        Throwable cause = authException.getCause();
        if (cause != null) {
            String message = cause.getMessage();
            if (message != null) errorMessage = message;
        }

        ApiResponse<Object> res = ApiResponse.builder()
                .statusCode(errorCode.getCode())
                .message(errorMessage)
                .error(errorCode.getMessage())
                .build();

        mapper.writeValue(response.getWriter(), res);
    }
}
