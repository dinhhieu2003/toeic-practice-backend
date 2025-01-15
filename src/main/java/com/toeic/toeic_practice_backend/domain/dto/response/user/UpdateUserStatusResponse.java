package com.toeic.toeic_practice_backend.domain.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusResponse {
	private String email;
	private boolean isActive;
}
