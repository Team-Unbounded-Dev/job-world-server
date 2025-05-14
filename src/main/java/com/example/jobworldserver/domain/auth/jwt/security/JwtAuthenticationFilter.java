package com.example.jobworldserver.domain.auth.jwt.security;

import com.example.jobworldserver.domain.auth.entity.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(JwtConstants.HEADER_STRING);

        if (header != null && header.startsWith(JwtConstants.TOKEN_PREFIX)) {
            String token = header.replace(JwtConstants.TOKEN_PREFIX, "");

            try {
                if (jwtService.validateToken(token) && !jwtService.isTokenBlacklisted(token)) {
                    User user = jwtService.getUserFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getNickname());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                log.error("JWT 인증 실패: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", e.getMessage());
                errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "서버 오류");
                errorResponse.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth/");
    }
}