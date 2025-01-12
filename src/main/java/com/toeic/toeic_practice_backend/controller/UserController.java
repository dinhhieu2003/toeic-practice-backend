package com.toeic.toeic_practice_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toeic.toeic_practice_backend.domain.dto.request.user.UpdateUserTargetRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.user.UserUpdateRoleRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.user.UserUpdateStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.auth.AccountResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.auth.AccountResponse.ResultOverview;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.LearningProgressResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UpdateUserTargetResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserInfoResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateStatusResponse;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.service.AccountService;
import com.toeic.toeic_practice_backend.service.LectureUserService;
import com.toeic.toeic_practice_backend.service.UserService;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.PaginationConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {
	private final UserService userService;
	private final AccountService accountService;
	
	private final LectureUserService lectureUserService;
	
	@PutMapping("/account/target")
	public ResponseEntity<UpdateUserTargetResponse> updateUserTarget(
			@RequestBody UpdateUserTargetRequest updateUserTargetRequest) {
		// Lấy thông tin Authentication hiện tại từ SecurityContextHolder
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    
	    // Kiểm tra xem authentication có null hoặc chưa xác thực hay không
	    if (authentication == null || !authentication.isAuthenticated()) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    
	    // Lấy email từ Authentication
	    String email = authentication.getName();
	    // Tìm kiếm người dùng theo email
	    Optional<User> userOptional = userService.getUserByEmailWithoutStat(email);
	    // Init UpdateUserTarget response
	    UpdateUserTargetResponse response = new UpdateUserTargetResponse();
	    if (userOptional.isPresent()) {
	    	User user = userOptional.get();
	    	response = userService.updateUserTarget(user.getId(), updateUserTargetRequest);
	    }
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/account")
	public ResponseEntity<AccountResponse> getCurrentAccount() {
		// Lấy thông tin Authentication hiện tại từ SecurityContextHolder
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    
	    // Kiểm tra xem authentication có null hoặc chưa xác thực hay không
	    if (authentication == null || !authentication.isAuthenticated()) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    
	    // Lấy email từ Authentication
	    String email = authentication.getName();
	    // Tìm kiếm người dùng theo email
	    Optional<User> userOptional = userService.getUserByEmail(email);
	    if (userOptional.isPresent()) {
	        User user = userOptional.get();
	        List<ResultOverview> listResultOverview = accountService.getResultOverview(user.getId());
	        AccountResponse accountResponse = AccountResponse.builder()
	        		.id(user.getId())
	                .email(user.getEmail())
	                .avatar(user.getAvatar())
	                .role(user.getRole())
	                .target(user.getTarget())
	                .overallStat(user.getOverallStat())
	                .topicStats(user.getTopicStats())
	                .skillStats(user.getSkillStats())
	                .learningProgress(user.getLearningProgress())
	                .results(listResultOverview)
	                .build();
	        return ResponseEntity.ok(accountResponse);
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }
	}
	
	@GetMapping("/lectures")
	public ResponseEntity<LearningProgressResponse> getLearningProgress() {
		return ResponseEntity.ok(lectureUserService.getLearningProgress());
	}
	
	@PutMapping("/{id}/role")
	public ResponseEntity<UserUpdateRoleResponse> updateUserRole(@PathVariable String id, @RequestBody UserUpdateRoleRequest role) {
		UserUpdateRoleResponse userUpdated = userService.updateUserRole(id, role.getRoleId());
		return ResponseEntity.ok(userUpdated);
	}
	
	@PutMapping("/{id}/status")
	public ResponseEntity<UserUpdateStatusResponse> updateUserStatus(@PathVariable String id, @RequestBody UserUpdateStatusRequest userUpdateStatusRequest) {
		UserUpdateStatusResponse userUpdated = userService.updateUserStatus(id,
				userUpdateStatusRequest.isActive());
		return ResponseEntity.ok(userUpdated);
	}
	
	@GetMapping
	public ResponseEntity<PaginationResponse<List<UserInfoResponse>>> getAllUser(
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_CURRENT_PAGE) int current,
			@RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
			@RequestParam(required = false ,defaultValue = "") String search) {
		Pageable pageable = PaginationUtils.createPageable(current, pageSize);
		return ResponseEntity.ok(userService.getAllUser(search, pageable));
	}
}
