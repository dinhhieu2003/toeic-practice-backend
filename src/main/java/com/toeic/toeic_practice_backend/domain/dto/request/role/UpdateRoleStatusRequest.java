package com.toeic.toeic_practice_backend.domain.dto.request.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleStatusRequest {
	private boolean active;
}
