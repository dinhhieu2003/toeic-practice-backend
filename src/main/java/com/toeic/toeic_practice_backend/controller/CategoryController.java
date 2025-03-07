package com.toeic.toeic_practice_backend.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.category.UpdateCategoryStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.category.GetCategoryResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.category.UpdateCategoryStatusResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.GetTestCardResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.service.CategoryService;
import com.toeic.toeic_practice_backend.service.TestCategoryService;
import com.toeic.toeic_practice_backend.service.TestService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryService categoryService;
	private final TestService testService;
	private final TestCategoryService testCategoryService;
	
	@PostMapping("")
	public ResponseEntity<Category> addCategory(@RequestBody Category category) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(categoryService.addCategory(category));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Category> updateCategory(@RequestBody Category category, @PathVariable String id) {
		return ResponseEntity.ok(categoryService.updateCategory(category, id));
	}
	
	@GetMapping("")
	public ResponseEntity<PaginationResponse<List<Category>>> getAllCategory(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam (required = false, defaultValue = "") String search) {
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(categoryService.getAllCategory(pageable, search));
	}
	
	@GetMapping("/none-page")
	public ResponseEntity<List<GetCategoryResponse>> getAllCategoryNonePage() {
		return ResponseEntity.ok(categoryService.getAllCategoryNonePage());
	}
	
	@GetMapping("/{categoryId}/tests")
	public ResponseEntity<PaginationResponse<List<Test>>> getTestsInCategory(
			@PathVariable String categoryId,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false, defaultValue = "") String search) {
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(testService.getTestsByCategoryId(search, categoryId, pageable));
	}
	
	@PutMapping("{categoryId}/status")
	public ResponseEntity<UpdateCategoryStatusResponse> updateCategoryStatus(
			@PathVariable String categoryId,
			@RequestBody UpdateCategoryStatusRequest upadateCategoryStatusRequest) {
		return ResponseEntity.ok(categoryService.updateCategoryStatus(categoryId, upadateCategoryStatusRequest));
	}
	
	// list test displayed for client
	@GetMapping("/tests")
	public ResponseEntity<PaginationResponse<List<GetTestCardResponse>>> getTestsByFormatAndYear(
			@RequestParam(defaultValue = "ETS") String format,
			@RequestParam(defaultValue = "") String year,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize) {
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(testCategoryService.getTestsByFormatAndYear(format, year, pageable));
	}
}
