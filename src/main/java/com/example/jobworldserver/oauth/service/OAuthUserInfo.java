package com.example.jobworldserver.oauth.service;

import com.example.jobworldserver.oauth.strategy.OAuthUserInfoStrategy;
import lombok.Getter;
import java.util.Map;

@Getter
public class OAuthUserInfo {
    private String providerId;
    private String provider;
    private String email;
    private String name;
    private String imageUrl;

    public OAuthUserInfo(String providerId, String provider, String email, String name, String imageUrl) {
        this.providerId = providerId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public static OAuthUserInfo of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        OAuthUserInfoStrategy strategy = OAuthUserInfoStrategy.getStrategy(registrationId);
        return strategy.createUserInfo(userNameAttributeName, attributes);
    }
}