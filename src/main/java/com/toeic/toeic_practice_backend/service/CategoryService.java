package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.CategoryRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
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
	
	public Category updateCategory(Category category, String id) {
        Category currentCategory = findById(id);
        currentCategory.setFormat(category.getFormat());
        currentCategory.setYear(category.getYear());
        Optional<Category> categoryOptional = categoryRepository
                .findByFormatAndYear(category.getFormat(), category.getYear());

        if (categoryOptional.isEmpty()) {
            return categoryRepository.save(currentCategory);
        } else {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
	}
	
	public PaginationResponse<List<Category>> getAllCategory(Pageable pageable) {
		Page<Category> categoryPage = categoryRepository.findAll(pageable);
		PaginationResponse<List<Category>> response = new PaginationResponse<List<Category>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(categoryPage.getTotalElements());
		meta.setTotalPages(categoryPage.getTotalPages());
		List<Category> result = categoryPage.getContent();
		response.setMeta(meta);
		response.setResult(result);
		return response;
	}
}