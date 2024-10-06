package com.toeic.toeic_practice_backend.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
	int statusCode;
	// message can be array 
	Object message;
	T data;
	String error;
}
