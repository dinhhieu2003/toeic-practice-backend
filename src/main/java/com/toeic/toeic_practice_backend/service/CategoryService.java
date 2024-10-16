package com.toeic.toeic_practice_backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.CategoryRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;
	public Category addCategory(String format, int year) {
		Optional<Category> categoryOptional = categoryRepository.findByFormatAndYear(format, year);
		Category categoryResponse = new Category();
		if(categoryOptional.isEmpty()) {
			Category newCategory = new Category();
			newCategory.setFormat(format);
			newCategory.setYear(year);
			newCategory.setActive(true);
			categoryResponse = categoryRepository.save(newCategory);
		} else {
			throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
		}
		return categoryResponse;
	}
	
	public Category findById(String id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
		return category;
	}
}