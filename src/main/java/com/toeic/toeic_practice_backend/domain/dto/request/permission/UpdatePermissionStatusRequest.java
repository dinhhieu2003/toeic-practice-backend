package com.toeic.toeic_practice_backend.domain.dto.request.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionStatusRequest {
	private boolean active;
}
