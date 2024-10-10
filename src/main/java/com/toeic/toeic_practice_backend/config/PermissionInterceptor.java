package com.toeic.toeic_practice_backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.toeic.toeic_practice_backend.domain.entity.Permission;
import com.toeic.toeic_practice_backend.domain.entity.Role;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.service.UserService;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.security.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PermissionInterceptor implements HandlerInterceptor {
    
	@Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, 
            Object handler
    ) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        String requestURI = request.getRequestURI();

        String httpMethod = request.getMethod();

        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        // check permission
        String email = SecurityUtils.getCurrentUserLogin().isPresent()
                ? SecurityUtils.getCurrentUserLogin().get()
                : "";
        if (!email.isEmpty()) {
            User user = this.userService.getUserByEmail(email).get();
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream().anyMatch(item -> item.getApiPath().equals(path)
                            && item.getMethod().equals(httpMethod));

                    if (!isAllow) {
                    	if(role.getName().equalsIgnoreCase("admin")) {
                    		isAllow = true;
                    	} else {
                    		throw new AppException(ErrorCode.UNAUTHORIZED);
                    	}    
                    }
                } else {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
            }
        }

        return true;
    }
}