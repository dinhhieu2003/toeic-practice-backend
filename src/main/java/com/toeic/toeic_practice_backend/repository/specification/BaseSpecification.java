package com.toeic.toeic_practice_backend.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public abstract class BaseSpecification<T> {
	protected String searchTerm;
    protected String[] sortBy;
    protected String[] sortDirection;
    protected Boolean active;
    
    public BaseSpecification(String searchTerm, String[] sortBy, String[] sortDirection, Boolean active) {
        this.searchTerm = searchTerm;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.active = active;
    }
    
    public Query buildQuery(Pageable pageable) {
        Query query = new Query();

        // Build search predicate
        Criteria searchCriteria = createSearchCriteria();
        if (searchCriteria != null) {
            query.addCriteria(searchCriteria);
        }

        // Add sort
        if (sortBy != null && sortDirection != null && sortBy.length == sortDirection.length) {
            List<Sort.Order> orders = new ArrayList<>();
            for (int i = 0; i < sortBy.length; i++) {
                Sort.Direction dir = sortDirection[i].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                orders.add(new Sort.Order(dir, sortBy[i]));
            }
            query.with(Sort.by(orders));
        }

        query.with(pageable);
        return query;
    }

    protected abstract Criteria createSearchCriteria();
}
