package com.example.jobworldserver.domain.auth.service.impl;

import com.example.jobworldserver.domain.auth.entity.Authority;
import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.jwt.constants.JwtConstants;
import com.example.jobworldserver.domain.auth.jwt.exception.JwtException;
import com.example.jobworldserver.domain.auth.jwt.token.JwtTokenProvider;
import com.example.jobworldserver.domain.auth.jwt.token.RefreshTokenStoreService;
import com.example.jobworldserver.domain.auth.jwt.token.TokenBlacklistService;
import com.example.jobworldserver.domain.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStoreService refreshTokenStoreService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public String createAccessToken(User user) {
        return jwtTokenProvider.generateToken(user, JwtConstants.ACCESS_TOKEN_EXPIRATION_TIME);
    }

    @Override
    public String createRefreshToken(User user) {
        String refreshToken = jwtTokenProvider.generateToken(user, JwtConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        refreshTokenStoreService.storeRefreshToken(user.getId(), refreshToken, JwtConstants.REFRESH_TOKEN_EXPIRATION_TIME);
        return refreshToken;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            throw JwtException.UNAUTHORIZED("토큰 유효성 검증 실패: " + e.getMessage());
        }
    }

    public String rotateRefreshToken(User user, String oldRefreshToken) {
        if (!validateToken(oldRefreshToken)) {
            throw JwtException.UNAUTHORIZED("유효하지 않은 Refresh Token입니다.");
        }

        String stored = refreshTokenStoreService.getRefreshToken(user.getId())
                .orElseThrow(() -> JwtException.UNAUTHORIZED("저장된 Refresh Token이 없습니다."));

        if (!stored.equals(oldRefreshToken)) {
            throw JwtException.UNAUTHORIZED("Refresh Token이 일치하지 않습니다.");
        }

        tokenBlacklistService.blacklistToken(oldRefreshToken, JwtConstants.REFRESH_TOKEN_EXPIRATION_TIME);

        String newRefreshToken = createRefreshToken(user);
        return newRefreshToken;
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isTokenBlacklisted(token);
    }

    @Override
    public User getUserFromToken(String token) {
        try {
            Claims claims = jwtTokenProvider.getClaims(token);
            return User.builder()
                    .id(Long.valueOf(claims.get("id").toString()))
                    .nickname(claims.getSubject())
                    .authority(Authority.valueOf(claims.get("authority").toString()))
                    .build();
        } catch (Exception e) {
            throw JwtException.FORBIDDEN("토큰에서 사용자 정보를 가져오지 못했습니다: " + e.getMessage());
        }
    }
}