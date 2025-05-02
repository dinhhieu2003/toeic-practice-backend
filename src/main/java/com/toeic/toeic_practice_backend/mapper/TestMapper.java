package com.toeic.toeic_practice_backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.toeic.toeic_practice_backend.domain.dto.response.test.GetTestCardResponse;
import com.toeic.toeic_practice_backend.domain.entity.Test;

@Component
@Mapper(componentModel = "spring")
public interface TestMapper {
	default List<GetTestCardResponse> listTestToListGetTestCardResponse(List<Test> tests, List<String> testIdsAttempt) {
		List<GetTestCardResponse> result = tests.stream()
                .map(test -> {
                    GetTestCardResponse testCardResponse = new GetTestCardResponse();
                    testCardResponse.setId(test.getId());
                    testCardResponse.setName(test.getName());
                    testCardResponse.setFormat(test.getCategory().getFormat());
                    testCardResponse.setYear(test.getCategory().getYear());
                    testCardResponse.setTotalUser(test.getTotalUserAttempt());
                    testCardResponse.setCompleted(testIdsAttempt.contains(test.getId()));
                    return testCardResponse;
                })
                .collect(Collectors.toList());
		return result;
	}
}
