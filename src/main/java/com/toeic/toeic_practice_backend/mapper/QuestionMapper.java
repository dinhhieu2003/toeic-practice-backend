package com.toeic.toeic_practice_backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import com.toeic.toeic_practice_backend.domain.dto.response.test.MultipleChoiceQuestion;
import com.toeic.toeic_practice_backend.domain.entity.Question;

@Component
@Mapper(componentModel = "spring")
public interface QuestionMapper {
	QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);
	
	List<MultipleChoiceQuestion> toListMultipleChoiceQuestionFromListQuestion(
			List<Question> listQuestion);
}
