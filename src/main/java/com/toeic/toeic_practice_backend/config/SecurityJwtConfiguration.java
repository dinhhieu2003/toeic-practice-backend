package com.toeic.toeic_practice_backend.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.security.JwtTokenUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityJwtConfiguration {

    private final RedisTemplate<String, String> redisTemplate;
	
	@Value("${toeic-practice-backend.security.authentication.jwt.base64-secret}")
	private String jwtKey;
	
	@Bean
    public JwtEncoder jwtEncoder() {
		log.info("Encoder");
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

	@Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(JwtTokenUtils.JWT_ALGORITHM).build();
        return token -> {
        	Jwt jwt;
            try {
            	jwt = jwtDecoder.decode(token);
            } catch (Exception e) {
                log.error(">>> JWT error: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.TOKEN_NOT_VALID);
            }
            
            String key = jwt.getId();
            // Check token absent in blacklist
        	if(redisTemplate.opsForValue().get(key) != null) {
        		log.error("Token {} is blacklisted", key);
        		throw new AppException(ErrorCode.TOKEN_NOT_VALID);
        	}
        	return jwt;
        };
    }
	
    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JwtTokenUtils.JWT_ALGORITHM.getName());
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
                JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        JwtAuthenticationConverter jwtAuthenticationConverter = new
                JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
