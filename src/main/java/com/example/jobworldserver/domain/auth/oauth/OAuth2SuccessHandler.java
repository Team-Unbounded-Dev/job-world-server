package com.example.jobworldserver.domain.auth.oauth;

import com.example.jobworldserver.domain.auth.entity.Authority;
import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.jwt.constants.JwtConstants;
import com.example.jobworldserver.domain.auth.repository.UserRepository;
import com.example.jobworldserver.domain.auth.service.JwtService;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final String REDIRECT_URI = "http://localhost:3000/oauth/callback"; // 프론트엔드 리다이렉트 URI

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        User user = extractUserFromOAuth2User(oAuth2User, response);
        if (user == null) {
            return; // 응답이 이미 처리되었으므로 메서드 종료
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
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        log.info("🔍 OAuth2 Success Handler에서 사용자 이메일 조회: {}", email);

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("사용자 없음, 새 사용자 등록: {}", email);
                    try {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName((String) attributes.get("name"));
                        newUser.setNickname(email.split("@")[0]);
                        newUser.setPassword("OAUTH2_USER");
                        newUser.setAuthority(Authority.NORMAL);
                        newUser.setEmailVerified(true);
                        newUser.setProvider("google");
                        newUser.setProviderId((String) attributes.get("sub"));
                        newUser.setProfileImageUrl((String) attributes.get("picture"));

                        User savedUser = userRepository.save(newUser);
                        log.info("새 사용자 등록 성공: {}", savedUser.getEmail());
                        return savedUser;
                    } catch (Exception e) {
                        log.error("새 사용자 등록 실패: {}", email, e);
                        try {
                            response.setContentType("application/json; charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    ApiResponse.failure("사용자 등록 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST)
                            ));
                        } catch (IOException ioException) {
                            log.error("에러 응답 작성 실패: {}", ioException.getMessage());
                        }
                        return null;
                    }
                });
    }
}