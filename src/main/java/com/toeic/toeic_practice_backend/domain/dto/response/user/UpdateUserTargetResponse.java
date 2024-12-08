package com.toeic.toeic_practice_backend.domain.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserTargetResponse {
	private String email;
	private int target;
}
