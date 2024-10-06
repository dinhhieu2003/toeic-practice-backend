package com.toeic.toeic_practice_backend.exception;

import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private ErrorCode errorCode;
	
	public AppException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}