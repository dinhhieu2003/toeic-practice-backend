package com.toeic.toeic_practice_backend.domain.dto.response.testDraft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckTestDraftExistResponse {
	private boolean exist;
	private Float version;
}
