package com.example.jobworldserver.domain.auth.jwt.exception;

import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.domain.auth.jwt.constants.JwtConstants;
import com.example.jobworldserver.domain.auth.service.CustomUserDetailsService;
import com.example.jobworldserver.domain.auth.service.JwtService;
import com.example.jobworldserver.domain.auth.jwt.exception.JwtException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt) && !jwtService.isTokenBlacklisted(jwt)) {
            try {
                String nickname = jwtService.getUserNicknameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(nickname);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 인증 성공: {}", nickname);
            } catch (JwtException e) {
                log.error("JWT 인증 실패: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                        ApiResponse.failure("JWT 인증 실패: " + e.getMessage(), HttpStatus.UNAUTHORIZED)
                ));
                return;
            } catch (Exception e) {
                log.error("서버 오류: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                        ApiResponse.failure("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
                ));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstants.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/job-world/login/oauth2/code/google") || uri.startsWith("/api/auth/");
    }
}