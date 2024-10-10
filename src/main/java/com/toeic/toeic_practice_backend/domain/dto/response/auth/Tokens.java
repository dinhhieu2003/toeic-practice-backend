package com.toeic.toeic_practice_backend.domain.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tokens {
	private String accessToken;
	private String refreshToken;
}
