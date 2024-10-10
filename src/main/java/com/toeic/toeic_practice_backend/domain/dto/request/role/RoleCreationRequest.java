package com.toeic.toeic_practice_backend.domain.dto.request.role;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreationRequest {
	private String name;
    private String description;
    private List<String> permissionIds = new ArrayList<>();
}