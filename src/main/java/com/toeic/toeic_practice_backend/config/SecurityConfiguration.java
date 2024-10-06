package com.toeic.toeic_practice_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.toeic.toeic_practice_backend.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.toeic.toeic_practice_backend.security.oauth2.OAuth2LoginSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(securedEnabled = false)
@RequiredArgsConstructor
public class SecurityConfiguration {
	
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final OAuth2LoginSuccessHandler successHandler;
	private final OAuth2AuthenticationFailureHandler failureHandler;
	private String[] whiteList = {"/api/v1/auth/**", "/oauth2/**", "/api/oauth2/**"};
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authz ->
					authz
						.requestMatchers(whiteList).permitAll()
						.anyRequest().authenticated()
			)
			.oauth2Login(auth -> {
                auth.authorizationEndpoint(point -> point.baseUri(
                        "/oauth2/authorize"));
                auth.redirectionEndpoint(redirect -> redirect.baseUri("/oauth2/callback/*"));
                auth.successHandler(successHandler);
                auth.failureHandler(failureHandler);
            })
			.oauth2ResourceServer(oauth2 -> 
			oauth2.jwt(Customizer.withDefaults())
					.authenticationEntryPoint(customAuthenticationEntryPoint));
//			.exceptionHandling(exceptions ->
//            	exceptions
//                	.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
//                	.accessDeniedHandler(new BearerTokenAccessDeniedHandler())
//				)
		return http.build();
	}
}
