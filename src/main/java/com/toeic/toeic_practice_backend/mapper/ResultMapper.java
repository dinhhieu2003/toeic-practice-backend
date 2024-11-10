package com.toeic.toeic_practice_backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultResponse;
import com.toeic.toeic_practice_backend.domain.entity.Result;

@Component
@Mapper(componentModel = "spring")
public interface ResultMapper {
    ResultMapper INSTANCE = Mappers.getMapper(ResultMapper.class);

    TestResultResponse toTestResultResponse(Result result);
    
    List<TestResultResponse> toTestResultResponseList(List<Result> results);
}
