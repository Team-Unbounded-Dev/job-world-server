package com.example.jobworldserver.domain.auth.service;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthUserInfo {
    private String providerId;
    private String provider;
    private String email;
    private String name;
    private String imageUrl;

    @Builder
    public OAuthUserInfo(String providerId, String provider, String email, String name, String imageUrl) {
        this.providerId = providerId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public static OAuthUserInfo of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
    }

    private static OAuthUserInfo ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthUserInfo.builder()
                .providerId(attributes.get(userNameAttributeName).toString())
                .provider("google")
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .imageUrl((String) attributes.get("picture"))
                .build();
    }
}