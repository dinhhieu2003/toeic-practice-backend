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

        // Lấy path và method từ request
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        // Debug log để kiểm tra giá trị đầu vào
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> requestURI= " + requestURI);
        System.out.println(">>> httpMethod= " + httpMethod);

        // Lấy email từ security context
        String email = SecurityUtils.getCurrentUserLogin()
                .filter(user -> !user.equals("anonymousUser"))
                .orElse("");

        // Nếu không có email hợp lệ, ném lỗi UNAUTHORIZED
        if (email.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy thông tin user và role
        User user = userService.getUserByEmailWithOnlyRole(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Role role = user.getRole();

        if (role == null || !role.isActive()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy danh sách permissions và kiểm tra quyền truy cập
        List<Permission> permissions = role.getPermissions();
        boolean isAllow = permissions.stream()
                .anyMatch(permission -> 
                    isApiMatched(permission.getApiPath(), path) && // Match path với mẫu động
                    permission.getMethod().equalsIgnoreCase(httpMethod) && // Match HTTP method
                    permission.isActive() // Quyền phải active
                );

        // Nếu không đủ quyền, kiểm tra trường hợp ngoại lệ cho admin
        if (!isAllow) {
            if (role.getName().equalsIgnoreCase("admin")) {
                isAllow = true;
            } else {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        return true; // Cho phép tiếp tục request
    }

    /**
     * So sánh API path với mẫu động hỗ trợ path variable.
     *
     * @param apiPath     Path trong Permission (có thể chứa {id} hoặc {variable})
     * @param requestPath Path thực tế từ request
     * @return true nếu match, ngược lại false
     */
    private boolean isApiMatched(String apiPath, String requestPath) {
        // Sử dụng regex để chuyển {variable} thành phần tử động
        String regex = apiPath.replaceAll("\\{\\w+}", "[^/]+");
        return requestPath.matches(regex);
    }
}