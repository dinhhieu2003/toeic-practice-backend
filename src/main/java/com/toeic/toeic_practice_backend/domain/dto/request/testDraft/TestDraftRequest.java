package com.toeic.toeic_practice_backend.domain.dto.request.testDraft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDraftRequest {
	private String testId;
	private String draftData;
	private Float version;
}
