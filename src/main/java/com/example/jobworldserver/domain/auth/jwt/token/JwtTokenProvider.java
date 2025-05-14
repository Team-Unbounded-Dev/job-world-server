package com.example.jobworldserver.domain.auth.jwt.token;

import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.jwt.constants.JwtConstants;
import com.example.jobworldserver.domain.auth.jwt.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key is not configured.");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        log.info("JWT secret key loaded successfully.");
    }

    public String generateToken(User user, long expirationMillis) {
        Claims claims = Jwts.claims()
                .subject(user.getNickname())
                .add("authority", user.getAuthority().name())
                .add("id", user.getId())
                .build();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 JWT 토큰: {}", e.getMessage(), e);
            throw JwtException.UNAUTHORIZED("유효하지 않은 JWT 토큰: " + e.getMessage());
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw JwtException.UNAUTHORIZED("토큰에서 클레임을 가져오지 못했습니다: " + e.getMessage());
        }
    }
}