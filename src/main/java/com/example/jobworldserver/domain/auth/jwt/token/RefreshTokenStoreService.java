package com.example.jobworldserver.domain.auth.jwt.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenStoreService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";

    public void storeRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public Optional<String> getRefreshToken(Long userId) {
        String token = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        return Optional.ofNullable(token);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }
}
