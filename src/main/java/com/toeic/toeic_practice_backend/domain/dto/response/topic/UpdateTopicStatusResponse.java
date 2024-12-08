package com.toeic.toeic_practice_backend.domain.dto.response.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopicStatusResponse {
	private String name;
	private boolean isActive;
}
