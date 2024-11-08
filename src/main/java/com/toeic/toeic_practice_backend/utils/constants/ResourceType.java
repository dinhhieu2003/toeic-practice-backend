package com.toeic.toeic_practice_backend.utils.constants;

import lombok.Getter;

@Getter
public enum ResourceType {
    AUDIO(0, "audio", "Âm thanh"),
	IMAGE(1, "image", "Hình ảnh"),
	PARAGRAPH(2, "paragraph", "Đoạn văn bản");
	
	private final int code;
	private final String name;
    private final String vName;
	
	ResourceType(int code, String name, String vName) {
		this.code = code;
		this.name = name;
        this.vName = vName;
	}
}
