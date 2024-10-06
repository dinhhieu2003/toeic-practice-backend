package com.toeic.toeic_practice_backend.utils.constants;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
	UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_KEY(1001, "Invalid key error", HttpStatus.BAD_REQUEST),
	EXISTED_USER(1002, "Email existed", HttpStatus.BAD_REQUEST),
	COMPANY_EXISTED(1003, "This company is existed", HttpStatus.BAD_REQUEST),
	
	
	UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
	UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN);
	
	private final int code;
	private final String message;
	private final HttpStatusCode statusCode;
	
	ErrorCode(int code, String message, HttpStatusCode statusCode) {
		this.code = code;
		this.message = message;
		this.statusCode = statusCode;
	}
	
	
}