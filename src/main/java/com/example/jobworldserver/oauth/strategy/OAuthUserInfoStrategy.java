package com.example.jobworldserver.oauth.strategy;

import com.example.jobworldserver.oauth.service.OAuthUserInfo;
import com.example.jobworldserver.oauth.strategy.impl.GoogleOAuthUserInfoStrategy;

import java.util.Map;

public interface OAuthUserInfoStrategy {
    OAuthUserInfo createUserInfo(String userNameAttributeName, Map<String, Object> attributes);
    static OAuthUserInfoStrategy getStrategy(String registrationId) {
        if ("google".equals(registrationId)) {
            return new GoogleOAuthUserInfoStrategy();
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }
}