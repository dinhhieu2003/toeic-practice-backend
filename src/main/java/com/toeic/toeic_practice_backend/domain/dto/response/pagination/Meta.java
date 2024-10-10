package com.toeic.toeic_practice_backend.domain.dto.response.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {
	int current;
    int pageSize;
    int totalPages;
    long totalItems;
}
