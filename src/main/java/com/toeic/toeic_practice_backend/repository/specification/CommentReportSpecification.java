package com.toeic.toeic_practice_backend.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;

import com.toeic.toeic_practice_backend.domain.entity.CommentReport;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;

public class CommentReportSpecification extends BaseSpecification<CommentReport> {
	
	private CommentTargetType commentContextType;
	private String commentContextId;
	
	public CommentReportSpecification(String searchTerm, String[] sortBy, String[] sortDirection, Boolean active,
			CommentTargetType commentContextType, String commentContextId) {
		super(searchTerm, sortBy, sortDirection, active);
		this.commentContextType = commentContextType;
		this.commentContextId = commentContextId;
	}

	@Override
	protected Criteria createSearchCriteria() {
		List<Criteria> criteriaList = new ArrayList<>();
		if (searchTerm != null && !searchTerm.isBlank()) {
            criteriaList.add(Criteria.where("reasonDetails").regex(".*" + Pattern.quote(searchTerm) + ".*", "i"));
        }

        if (active != null) {
            criteriaList.add(Criteria.where("isActive").is(active));
        }
        
        if (commentContextId != null) {
            criteriaList.add(Criteria.where("commentContextId").is(commentContextId));
        }

        if (commentContextType != null) {
            criteriaList.add(Criteria.where("commentContextType").is(commentContextType));
        }
        
        if (criteriaList.isEmpty()) return null;

        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
	}
	
}
