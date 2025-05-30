package com.toeic.toeic_practice_backend.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import com.toeic.toeic_practice_backend.domain.dto.response.category.GetCategoryResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;

@Component
@Mapper(componentModel = "spring")
public interface CategoryMapper {
	CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);
	
	default List<GetCategoryResponse> listCategoryToListGetCategoryResponse(List<Category> categories) {
		List<GetCategoryResponse> listGetCategoryResponse = categories.stream()
		        .collect(Collectors.groupingBy(Category::getFormat))
		        .entrySet().stream()
		        .map(entry -> {
		            GetCategoryResponse responseItem = new GetCategoryResponse();
		            responseItem.setFormat(entry.getKey());
		            responseItem.setYear(entry.getValue().stream()
		                                       .map(Category::getYear)
		                                       .distinct() // Loại bỏ các năm trùng lặp nếu cần
		                                       .sorted(Comparator.reverseOrder())
		                                       .collect(Collectors.toList()));
		            return responseItem;
		        })
		        .collect(Collectors.toList());
		return listGetCategoryResponse;
	}
}
