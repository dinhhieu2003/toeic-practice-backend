package com.toeic.toeic_practice_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/",
                "/api/v1/auth/**",
                "/oauth2/**", 
                "/api/oauth2/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                
                // guest
                "/api/v1/categories/**",
        		"/api/v1/tests/**",
        		"/api/v1/questions/**",
        		"/api/v1/lectures/**"
        };
        
        // Add the interceptor to all paths, excluding the whitelist
        // Note: Internal API paths (/api/v1/internal/**) are NOT in the whitelist,
        // so they will be intercepted and validated by the PermissionInterceptor
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}