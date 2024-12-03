package com.toeic.toeic_practice_backend.domain.dto.response.user;

import com.toeic.toeic_practice_backend.domain.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
	private String id;
    private String email;
    private Role role;
    private int target;
    private boolean isActive;
}
