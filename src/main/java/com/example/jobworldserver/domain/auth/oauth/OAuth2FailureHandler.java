package com.example.jobworldserver.domain.auth.oauth;

import com.example.jobworldserver.dto.auth.common.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 로그인 실패: {}", exception.getMessage());
        log.error("요청 URL: {}", request.getRequestURL());
        log.error("파라미터: {}", request.getQueryString());
        ApiResponse.failure("OAuth2 인증 실패: " + exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}