package com.toeic.toeic_practice_backend.domain.dto.request.lecture;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLectureRequest {
    private String name;
    private String content;
    private List<String> topicIds = new ArrayList<>();
}
