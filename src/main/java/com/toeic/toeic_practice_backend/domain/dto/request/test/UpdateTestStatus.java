package com.toeic.toeic_practice_backend.domain.dto.request.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTestStatus {
	private boolean active;
}
