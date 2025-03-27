package com.toeic.toeic_practice_backend.utils.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.config.SecurityJwtConfiguration;
import com.toeic.toeic_practice_backend.domain.dto.response.auth.UserToken;
import com.toeic.toeic_practice_backend.domain.entity.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Getter
public class JwtTokenUtils {
	public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
	@Value("${toeic-practice-backend.security.authentication.jwt.base64-secret}")
	private String jwtKey;
	
	@Value("${toeic-practice-backend.security.authentication.jwt.token-validity-in-seconds}")
	private long accessTokenExpiration;
	@Value("${toeic-practice-backend.security.authentication.jwt.refreshToken-validity-in-seconds}")
	private long refreshTokenExpiration;
	
	private final JwtEncoder jwtEncoder;
	private final SecurityJwtConfiguration jwtConfiguration;
	
	public String createAccessToken(User user) {
		Instant now = Instant.now();
        Instant validity = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

//        User information in token
        UserToken userToken = new UserToken();
        userToken.setId(user.getId());
        userToken.setEmail(user.getEmail());
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getEmail())
                .claim("user", userToken)
                .claim("role", user.getRole().getName())
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
                claims)).getTokenValue();
	}
	
	public String createRefreshToken(User user) {
		Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        //        User information in token
        UserToken userToken = new UserToken();
        userToken.setId(user.getId());
        userToken.setEmail(user.getEmail());

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getEmail())
                .claim("user", userToken)
                .claim("role", user.getRole().getName())
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
                claims)).getTokenValue();
	}
	
	public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                this.jwtConfiguration.getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(refreshToken);
        } catch (Exception e) {
            System.out.println(">>> JWT refresh error: " + e.getMessage());
            throw e;
        }
    }
}
