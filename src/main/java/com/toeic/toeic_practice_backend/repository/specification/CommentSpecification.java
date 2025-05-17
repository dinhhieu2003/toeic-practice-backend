package com.toeic.toeic_practice_backend.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import com.toeic.toeic_practice_backend.domain.entity.Comment;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;

public class CommentSpecification extends BaseSpecification<Comment> {

	private CommentTargetType commentTargetType;
	private String targetId;
	private String parentId;
	private boolean filteredByParentId;
	public CommentSpecification(String searchTerm, String[] sortBy, String[] sortDirection, Boolean active,
			CommentTargetType commentTargetType, String targetId, String parentId, boolean filteredByParentId) {
		super(searchTerm, sortBy, sortDirection, active);
		this.commentTargetType = commentTargetType;
		this.targetId = targetId;
		this.parentId = parentId;
		this.filteredByParentId = filteredByParentId;
	}

	@Override
	protected Criteria createSearchCriteria() {
		List<Criteria> criteriaList = new ArrayList<>();
		if (searchTerm != null && !searchTerm.isBlank()) {
            criteriaList.add(Criteria.where("content").regex(".*" + Pattern.quote(searchTerm) + ".*", "i"));
        }

        if (active != null) {
            criteriaList.add(Criteria.where("isActive").is(active));
        }
        
        if(filteredByParentId) {
        	if (parentId != null) {
            	System.out.println("ParentId: " + parentId);
                criteriaList.add(Criteria.where("parentId").is(parentId));
            } else {
            	// get root comments
                criteriaList.add(new Criteria().orOperator(
                    Criteria.where("parentId").is(null),
                    Criteria.where("parentId").is("")
                ));
            }
        }
        

        if (targetId != null) {
            criteriaList.add(Criteria.where("targetId").is(targetId));
        }

        if (commentTargetType != null) {
            criteriaList.add(Criteria.where("targetType").is(commentTargetType));
        }

        if (criteriaList.isEmpty()) return null;

        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    }

}
