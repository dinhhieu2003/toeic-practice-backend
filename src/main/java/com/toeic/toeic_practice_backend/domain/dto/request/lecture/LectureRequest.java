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
public class LectureRequest {
    private String id;
    private String name;
    private String content;
    private List<String> topicIds = new ArrayList<>();
    private List<LecturePractice> practiceQuestions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LecturePractice {
        private String type;
        private List<LecturePractice> subQuestions = new ArrayList<>();
        private String content;
        private List<String> topicIds = new ArrayList<>();
        private List<Resource> resources;
        private String transcript;
        private String explanation;
        private List<String> answers= new ArrayList<>();
        private String correctAnswer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Resource {
        private String type;  // paragraph, image, or audio
        private String content;
    }
}
