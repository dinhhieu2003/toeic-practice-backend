package com.toeic.toeic_practice_backend.domain.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserTargetRequest {
	private int target;
}
