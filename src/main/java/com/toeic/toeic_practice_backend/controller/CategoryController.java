package com.toeic.toeic_practice_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryService categoryService;
	
	@PostMapping("")
	public ResponseEntity<Category> addCategory(@RequestBody Category category) {
		String format = category.getFormat();
		int year = category.getYear();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(categoryService.addCategory(format, year));
	}
	
	@PostMapping("/{id}")
	public ResponseEntity<Category> updateCategory(@RequestBody Category category, @PathVariable String id) {
		return ResponseEntity.ok(categoryService.updateCategory(category, id));
	}
}
