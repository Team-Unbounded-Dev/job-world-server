package com.example.jobworldserver.oauth.handler;

import com.example.jobworldserver.auth.service.JwtService;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.auth.service.CustomOAuth2UserService;
import com.example.jobworldserver.oauth.service.OAuthUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${oauth2.redirect-uri:http://localhost:3000/oauth/callback}")
    private String REDIRECT_URI;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = extractUserFromOAuth2User(oAuth2User, response);
        if (user == null) {
            return;
        }

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        log.info("OAuth2 로그인 성공: 사용자={}, 리다이렉트={}", user.getEmail(), targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User extractUserFromOAuth2User(OAuth2User oAuth2User, HttpServletResponse response) throws IOException {
        String email = oAuth2User.getAttribute("email");
        log.info("🔍 OAuth2 Success Handler에서 사용자 이메일 조회: {}", email);

        if (email == null) {
            log.error("OAuth2 사용자 이메일을 가져올 수 없음");
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.failure("사용자 이메일을 가져올 수 없습니다.", HttpStatus.BAD_REQUEST)
            ));
            return null;
        }

        // Delegate user creation or retrieval to CustomOAuth2UserService
        OAuthUserInfo userInfo = OAuthUserInfo.of("google", "sub", oAuth2User.getAttributes());
        return customOAuth2UserService.saveOrUpdateUser(userInfo);
    }
}