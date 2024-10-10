package com.toeic.toeic_practice_backend.domain.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
	private String id;
	private String email;
	private String avatar;
	private String accessToken;
}
