package com.toeic.toeic_practice_backend.domain.dto.response.comment;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentClassificationResponse {
	@JsonProperty("classification_probabilities")
	private ClassificationType classificationProbabilities;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ClassificationType {
		@JsonProperty("INSULT")
        private float insult;

        @JsonProperty("THREAT")
        private float threat;

        @JsonProperty("HATE_SPEECH")
        private float hateSpeech;

        @JsonProperty("SPAM")
        private float spam;

        @JsonProperty("SEVERE_TOXICITY")
        private float severeToxicity;

        @JsonProperty("OBSCENE")
        private float obscene;
	}
}
