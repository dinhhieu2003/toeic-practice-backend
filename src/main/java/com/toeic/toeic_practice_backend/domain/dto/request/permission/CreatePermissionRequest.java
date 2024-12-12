package com.toeic.toeic_practice_backend.domain.dto.request.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {
	private String name;
	private String apiPath;
	private String method;
	private String module;
}
