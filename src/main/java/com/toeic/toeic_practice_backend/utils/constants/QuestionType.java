package com.toeic.toeic_practice_backend.utils.constants;

import lombok.Getter;

@Getter
public enum QuestionType {
    SINGLE(0, "single", "Câu hỏi đơn"),
	GROUP(1, "group", "Câu hỏi nhóm"),
	SUBQUESTION(2, "subquestion", "Câu hỏi thành phần");
	
	private final int code;
	private final String name;
    private final String vName;
	
	QuestionType(int code, String name, String vName) {
		this.code = code;
		this.name = name;
		this.vName = vName;
	}
}
