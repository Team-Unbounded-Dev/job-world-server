package com.example.jobworldserver.auth.jwt.token;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.auth.jwt.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString()) // ID를 subject로 설정
                .claim("nickname", user.getNickname()) // 닉네임 추가
                .claim("authority", user.getAuthority().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
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

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getUserNicknameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("nickname", String.class);
    }
}