package com.toeic.toeic_practice_backend.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;

import com.toeic.toeic_practice_backend.domain.entity.CommentReport;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportReasonCategory;
import com.toeic.toeic_practice_backend.utils.constants.CommentReportStatus;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;

public class CommentReportSpecification extends BaseSpecification<CommentReport> {
	
	private CommentTargetType commentContextType;
	private String commentContextId;
	private CommentReportStatus status;
	private CommentReportReasonCategory reasonCategory;
	
	public CommentReportSpecification(String searchTerm, String[] sortBy, String[] sortDirection, Boolean active,
			CommentTargetType commentContextType, String commentContextId, 
			CommentReportStatus status, CommentReportReasonCategory reasonCategory) {
		super(searchTerm, sortBy, sortDirection, active);
		this.commentContextType = commentContextType;
		this.commentContextId = commentContextId;
		this.status = status;
		this.reasonCategory = reasonCategory;
	}

	@Override
	protected Criteria createSearchCriteria() {
		List<Criteria> criteriaList = new ArrayList<>();
		if (searchTerm != null && !searchTerm.isBlank()) {
			List<Criteria> orCriteria = new ArrayList<>();
		    orCriteria.add(Criteria.where("_id").is(searchTerm));
		    orCriteria.add(Criteria.where("reasonDetails").regex(".*" + Pattern.quote(searchTerm) + ".*", "i"));
		    orCriteria.add(Criteria.where("adminNotes").regex(".*" + Pattern.quote(searchTerm) + ".*", "i"));
		    criteriaList.add(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
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
        
        if(status != null ) {
        	criteriaList.add(Criteria.where("status").is(status));
        }
        
        if(reasonCategory != null ) {
        	criteriaList.add(Criteria.where("reasonCategory").is(reasonCategory));
        }
        
        if (criteriaList.isEmpty()) return null;

        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
	}
	
}
