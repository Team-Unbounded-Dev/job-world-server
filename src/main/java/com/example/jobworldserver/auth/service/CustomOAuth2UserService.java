package com.example.jobworldserver.auth.service;

import com.example.jobworldserver.auth.entity.Authority;
import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.oauth.service.OAuthUserInfo;
import com.example.jobworldserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("OAuth2 로그인 처리 중: provider={}, attributes={}", registrationId, attributes);

        OAuthUserInfo userInfo = OAuthUserInfo.of(registrationId, userNameAttributeName, attributes);
        if (userInfo.getEmail() == null) {
            throw new OAuth2AuthenticationException("OAuth2 제공자로부터 이메일을 가져올 수 없습니다.");
        }
        User user = saveOrUpdateUser(userInfo);

        Map<String, Object> customAttributes = new HashMap<>(attributes);
        customAttributes.put("id", user.getId());
        customAttributes.put("authority", user.getAuthority().name());
        customAttributes.put("email", user.getEmail());
        user.setAttributes(customAttributes);

        return user;
    }

    @Transactional
    public User saveOrUpdateUser(OAuthUserInfo userInfo) {
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            user.setProvider(userInfo.getProvider());
            user.setProviderId(userInfo.getProviderId());
            user.setProfileImageUrl(userInfo.getImageUrl());
            user.setEmailVerified(true);

            log.info(" 기존 OAuth 사용자 정보 업데이트: {}", user.getEmail());

            return userRepository.save(user);
        } else {
            String randomPassword = UUID.randomUUID().toString();
            String nickname = generateUniqueNickname(userInfo.getName());

            User newUser = User.builder()
                    .name(userInfo.getName())
                    .nickname(nickname)
                    .password(passwordEncoder.encode(randomPassword))
                    .email(userInfo.getEmail())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .emailVerified(true)
                    .authority(Authority.NORMAL)
                    .profileImageUrl(userInfo.getImageUrl())
                    .build();

            log.info(" 새로운 OAuth 사용자 저장: {}", newUser.getEmail());

            return userRepository.save(newUser);
        }
    }

    private String generateUniqueNickname(String name) {
        String baseNickname = name.replaceAll("\\s+", "");
        String nickname = baseNickname;
        int count = 1;

        while (userRepository.findByNickname(nickname).isPresent()) {
            nickname = baseNickname + count++;
        }

        return nickname;
    }
}