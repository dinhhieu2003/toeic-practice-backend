package com.toeic.toeic_practice_backend.domain.dto.request.chatgpt;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public record ChatGPTRequest(String model, List<Message> messages) {
    public static record Message(String role, List<ContentPart> content) {}
    @JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"
    )
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = TextPart.class, name = "text"),
	    @JsonSubTypes.Type(value = ImageUrlPart.class, name = "image_url")
	})
    public interface ContentPart {}
    
    public static record TextPart(String text)  implements ContentPart {}
    
    public static record ImageUrlPart(ImageUrl image_url) implements ContentPart{}
    
    public static record ImageUrl(String url, String detail) {}
    
}