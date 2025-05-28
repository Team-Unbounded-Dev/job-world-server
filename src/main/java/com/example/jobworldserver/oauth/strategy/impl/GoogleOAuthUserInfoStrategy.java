package com.example.jobworldserver.oauth.strategy.impl;

import com.example.jobworldserver.oauth.service.OAuthUserInfo;
import com.example.jobworldserver.oauth.strategy.OAuthUserInfoStrategy;
import java.util.Map;

public class GoogleOAuthUserInfoStrategy implements OAuthUserInfoStrategy {
    @Override
    public OAuthUserInfo createUserInfo(String userNameAttributeName, Map<String, Object> attributes) {
        return new OAuthUserInfo(
                attributes.get(userNameAttributeName).toString(),
                "google",
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }
}