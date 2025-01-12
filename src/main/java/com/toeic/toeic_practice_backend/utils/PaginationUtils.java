package com.toeic.toeic_practice_backend.utils;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;

public class PaginationUtils {
	public static Pageable createPageable(int current, int pageSize) {
        int currentPageValid = Math.max(current - 1, 0);
        int pageSizeValid = Math.max(pageSize, 1);
        return PageRequest.of(currentPageValid, pageSizeValid);
    }
	
	public static <T> PaginationResponse<List<T>> buildPaginationResponse(Pageable pageable, Page<T> pageData) {
		PaginationResponse<List<T>> response = new PaginationResponse<List<T>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(pageData.getTotalElements());
		meta.setTotalPages(pageData.getTotalPages());
		List<T> result = pageData.getContent();
		response.setMeta(meta);
		response.setResult(result);
		return response;
	}
	
	public static <T> PaginationResponse<List<T>> buildPaginationResponse(Meta meta, List<T> result) {
		PaginationResponse<List<T>> response = new PaginationResponse<List<T>>();
		response.setMeta(meta);
		response.setResult(result);
		return response;
	}
}
