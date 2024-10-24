package com.toeic.toeic_practice_backend.utils.constants;

import lombok.Getter;

@Getter
public enum Difficulty {
    EASY(0, "easy", "Dễ"),
	MEDIUM(1, "medium", "Trung bình"),
	HARD(2, "hard", "Khó");
	
	private final int code;
	private final String name;
    private final String vName;
	
	Difficulty(int code, String name, String vName) {
		this.code = code;
		this.name = name;
        this.vName = vName;
	}
}
