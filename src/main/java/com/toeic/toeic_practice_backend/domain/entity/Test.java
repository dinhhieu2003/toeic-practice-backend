package com.toeic.toeic_practice_backend.domain.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "tests")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Test extends BaseEntity{
    @Id
    private String id;
    private String name;
    private String format;
    private String totalUserAttempt;
    private int totalQuestion;
    private int totalScore;
    private String timeLimit;
    private String fullTestAudioUrl;
    private List<Part> parts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String number;
        private List<QuestionGroup> questionGroups;
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class QuestionGroup {
            private String audioUrl;
            private List<String> imageUrls;
            private List<String> paragraphs;
            private List<String> questionIds;
            private String transcript;
        }
    }
}