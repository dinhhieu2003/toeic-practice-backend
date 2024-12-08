package com.toeic.toeic_practice_backend.domain.dto.request.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopicStatusRequest {
	private boolean active;
}
