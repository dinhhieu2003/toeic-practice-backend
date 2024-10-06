package com.toeic.toeic_practice_backend.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.toeic.toeic_practice_backend.domain.dto.response.ApiResponse;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;


@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(value = AppException.class)
	public ResponseEntity<ApiResponse<Object>> handleException(AppException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		ApiResponse<Object> res = new ApiResponse<>();
        res.setStatusCode(errorCode.getCode());
        res.setMessage(errorCode.getMessage());
        res.setError(HttpStatus.valueOf(errorCode.getStatusCode().value()).name());
        return ResponseEntity.status(errorCode.getStatusCode()).body(res);
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Object>> handleBadCredentialException(BadCredentialsException ex) {
		ApiResponse<Object> res = new ApiResponse<>();
		res.setStatusCode(403);
		res.setMessage(ex.getMessage());
		res.setError("Bad credentials");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationError(MethodArgumentNotValidException ex) {
		BindingResult result = ex.getBindingResult();
		final List<FieldError> fieldErrors = result.getFieldErrors();
		List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
		
		ApiResponse<Object> res = new ApiResponse<>();
		res.setStatusCode(HttpStatus.BAD_REQUEST.value());
		res.setMessage(errors.size() > 1 ? errors : errors.get(0));
		res.setError(ex.getBody().getDetail());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
	}
}