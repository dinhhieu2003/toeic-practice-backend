package com.toeic.toeic_practice_backend.domain.dto.response.category;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCategoryResponse {
	private String format;
	private List<Integer> year = new ArrayList<>();
}
