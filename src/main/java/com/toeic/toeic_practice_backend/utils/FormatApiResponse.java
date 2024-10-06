package com.toeic.toeic_practice_backend.utils;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.toeic.toeic_practice_backend.domain.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class FormatApiResponse implements ResponseBodyAdvice<Object>{
	
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		System.out.println("Return type: " + returnType);
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
		int status = servletResponse.getStatus();
		if(status >= 400) {
			return body;
		}
		ApiResponse<Object> apiResponse = new ApiResponse<>();
		apiResponse.setStatusCode(status);
		apiResponse.setMessage("Call api");
		apiResponse.setData(body);
		return apiResponse;
	}

}