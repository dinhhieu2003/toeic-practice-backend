package com.toeic.toeic_practice_backend.domain.dto.response.lecture;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureCardResponse {
	private String id;
	private String name;
	private List<String> topic = new ArrayList<>();
	private int percent;
}
