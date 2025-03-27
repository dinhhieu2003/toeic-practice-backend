package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toeic.toeic_practice_backend.domain.dto.request.category.UpdateCategoryStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.category.GetCategoryResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.category.UpdateCategoryStatusResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.CategoryMapper;
import com.toeic.toeic_practice_backend.repository.CategoryRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class CategoryService {
	private final Logger log = LoggerFactory.getLogger(CategoryService.class);
	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;
	
	public Category addCategory(Category category) {
		log.info("Start: Function add new category");
		
		String format = category.getFormat();
		int year = category.getYear();
		Optional<Category> categoryOptional = categoryRepository.findByFormatAndYear(format, year);
		
		if(categoryOptional.isPresent()) {
			log.error("Error: Category with format [{}] and year [{}] is existed", format, year);
			throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
		}
		Category newCategory = new Category();
        newCategory.setFormat(format);
        newCategory.setYear(year);
        newCategory.setActive(true);
        
        Category savedCategory = categoryRepository.save(newCategory);
        log.info("End: Function add category success with id: {}", savedCategory.getId());
		return savedCategory;
	}
	
	public Category findById(String id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
		return category;
	}
	
	public Category updateCategory(Category category, String id) {
		log.info("Start: Update an existed category");
        Category currentCategory = findById(id);
        String format = category.getFormat();
        int year = category.getYear();
    
        Optional<Category> categoryOptional = categoryRepository
                .findByFormatAndYear(format, year);

        if(categoryOptional.isPresent()) {
        	log.error("Error: Category with format [{}] and year [{}] is existed", format, year);
        	throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        currentCategory.setFormat(format);
        currentCategory.setYear(year);
        Category updatedCategory = categoryRepository.save(currentCategory);
        log.info("End: Update category success with id {}", updatedCategory.getId());
        return updatedCategory;
	}
	
	public UpdateCategoryStatusResponse updateCategoryStatus(String categoryId, UpdateCategoryStatusRequest updateCategoryStatusRequest) {
		log.info("Start: Update category status");
		Category currentCategory = findById(categoryId);
		currentCategory.setActive(updateCategoryStatusRequest.isActive());
		Category newCategory = categoryRepository.save(currentCategory);
		log.info("End: Update category status success");
		UpdateCategoryStatusResponse updateCategoryStatusResponse = new UpdateCategoryStatusResponse();
		updateCategoryStatusResponse.setId(newCategory.getId());
		updateCategoryStatusResponse.setFormat(newCategory.getFormat());
		updateCategoryStatusResponse.setYear(newCategory.getYear());
		updateCategoryStatusResponse.setActive(newCategory.isActive());
		return updateCategoryStatusResponse;
	}
	
	public PaginationResponse<List<Category>> getAllCategory(Pageable pageable, String search) {
		log.info("Start: Function get all category");
		Page<Category> categoryPage = null;
		if(search.isEmpty()) {
			log.info("Search term is empty - finding all categories in database");
			categoryPage = categoryRepository.findAll(pageable);
		} else {
			log.info("Finding all categories by search term");
			categoryPage = categoryRepository.findByFormatContaining(search, pageable);
		}
		PaginationResponse<List<Category>> response = 
				PaginationUtils.buildPaginationResponse(pageable, categoryPage);
		log.info("End: Function get all categories");
		return response;
	}
	
	public List<GetCategoryResponse> getAllCategoryNonePage() {
		log.info("Start: Function get all category by format list year");
		List<Category> listCategory = categoryRepository.findByIsActiveTrue();
		List<GetCategoryResponse> listGetCategoryResponse = categoryMapper.listCategoryToListGetCategoryResponse(listCategory);
		return listGetCategoryResponse;
	}
	
	
	public List<Category> getAllByFormat(String format) {
		return categoryRepository.findByFormatAndIsActiveTrue(format);
	}
}