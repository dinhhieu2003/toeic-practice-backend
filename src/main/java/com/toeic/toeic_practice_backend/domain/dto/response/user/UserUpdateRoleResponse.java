package com.toeic.toeic_practice_backend.domain.dto.response.user;

import com.toeic.toeic_practice_backend.domain.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRoleResponse {
	private String email;
	private Role role;
}
