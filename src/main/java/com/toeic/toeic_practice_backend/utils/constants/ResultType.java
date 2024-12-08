package com.toeic.toeic_practice_backend.utils.constants;

import lombok.Getter;

@Getter
public enum ResultType {
    PRACTICE(0, "practice", "Luyện tập"),
	FULLTEST(1, "fulltest", "Luyện thi"),
	EXERCISE(2, "exercise", "Bài tập");
	private final int code;
	private final String name;
    private final String vName;
	
	ResultType(int code, String name, String vName) {
		this.code = code;
		this.name = name;
        this.vName = vName;
	}
}
