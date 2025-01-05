package com.toeic.toeic_practice_backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.GetTestCardResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Test;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestCategoryService {
	private final TestService testService;
	private final CategoryService categoryService;
	private final UserService userService;
	
	public PaginationResponse<List<GetTestCardResponse>> getTestsByFormatAndYear(
			String format, String year, Pageable pageable) {
		int yearInt = 0;
		if(!year.isEmpty()) {
			yearInt = Integer.parseInt(year);
		}
		Page<Test> testPage = null;
		if(yearInt == 0) {
			// find all categories active by format
			List<Category> listCategory = categoryService.getAllByFormat(format);
			// find all test by list category ids
			List<String> listCategoryId = listCategory.stream()
					.map(Category::getId)
					.collect(Collectors.toList());
			testPage = testService.getTestsByCategoryId(listCategoryId, pageable);
		} else {
			testPage = testService
					.getTestsByFormatAndYear(format, yearInt, pageable);
		}
		PaginationResponse<List<GetTestCardResponse>> response = new PaginationResponse<List<GetTestCardResponse>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(testPage.getTotalElements());
		meta.setTotalPages(testPage.getTotalPages());
		List<Test> listTest = testPage.getContent();
		HashSet<String> testIds = new HashSet<>();
		// Lấy thông tin Authentication hiện tại từ SecurityContextHolder
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null && authentication.isAuthenticated() 
	            && !(authentication instanceof AnonymousAuthenticationToken)) {
	    	System.out.println(authentication);
	    	String email = authentication.getName();
	    	testIds.addAll(userService.getUserTestHistory(email));
	    }
		
		List<GetTestCardResponse> result = listTest.stream()
                .map(test -> {
                    GetTestCardResponse testCardResponse = new GetTestCardResponse();
                    testCardResponse.setId(test.getId());
                    testCardResponse.setName(test.getName());
                    testCardResponse.setFormat(test.getCategory().getFormat());
                    testCardResponse.setYear(test.getCategory().getYear());
                    testCardResponse.setTotalUser(test.getTotalUserAttempt());
                    testCardResponse.setCompleted(testIds.contains(test.getId()));
                    return testCardResponse;
                })
                .collect(Collectors.toList());
		response.setMeta(meta);
		response.setResult(result);
		return response;
	}
}
