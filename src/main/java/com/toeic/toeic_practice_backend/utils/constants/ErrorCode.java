package com.toeic.toeic_practice_backend.utils.constants;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
	UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_KEY(1001, "Invalid key provided", HttpStatus.BAD_REQUEST),
	USER_ALREADY_EXISTS(1002, "Email already exists", HttpStatus.BAD_REQUEST),
	UNAUTHENTICATED(1003, "Unauthenticated access", HttpStatus.UNAUTHORIZED),
	UNAUTHORIZED(1004, "You do not have permission", HttpStatus.FORBIDDEN),
	TOKEN_NOT_VALID(1005, "Token is not valid", HttpStatus.BAD_REQUEST),
	MISSING_COOKIE(1006, "Required cookie is missing", HttpStatus.BAD_REQUEST),
	
//	Category
	CATEGORY_ALREADY_EXISTS(2001, "Category already exists", HttpStatus.BAD_REQUEST),
	CATEGORY_NOT_FOUND(2002, "Category not found", HttpStatus.NOT_FOUND),
	
//	Test 
	TEST_ALREADY_EXISTS(3001, "Test already exists", HttpStatus.BAD_REQUEST),
	TEST_NOT_FOUND(3002, "Test not found", HttpStatus.NOT_FOUND),
	
//	Topic
	TOPIC_ALREADY_EXISTS(4001, "Topic already exists", HttpStatus.BAD_REQUEST),
	TOPIC_NOT_FOUND(4002, "Topic not found", HttpStatus.NOT_FOUND),
	
//	User
	USER_NOT_FOUND(5001, "Email not found", HttpStatus.NOT_FOUND),
	
//	Question
	QUESTION_NOT_FOUND(6001, "Question not found", HttpStatus.NOT_FOUND),
	EXCEL_IMPORT_ERROR(6002, "Error importing Excel at row: %s", HttpStatus.BAD_REQUEST),
	CEL_IMPORT_ERROR(6003, "ERROR in a cell with message: %s", HttpStatus.BAD_REQUEST),

//	Result
	RESULT_NOT_FOUND(7001, "Result not found", HttpStatus.NOT_FOUND),

//  Lecture
	LECTURE_NOT_FOUND(8001, "Lecture not found", HttpStatus.NOT_FOUND),
	INDEX_OUT_BOUND(8002, "Index practice is out of bound", HttpStatus.BAD_REQUEST),
	
//	Permission
	PERMISSION_NOT_FOUND(9001, "Permission not found", HttpStatus.NOT_FOUND),
	
//	Role
	ROLE_NOT_FOUND(10001, "Role not found", HttpStatus.NOT_FOUND);
	
	private final int code;
	private final String message;
	private final HttpStatusCode statusCode;
	
	ErrorCode(int code, String message, HttpStatusCode statusCode) {
		this.code = code;
		this.message = message;
		this.statusCode = statusCode;
	}
	
	public String formatMessage(Object... args) {
		return String.format(this.message, args);
	}
}