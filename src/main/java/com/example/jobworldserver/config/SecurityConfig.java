package com.example.jobworldserver.config;

import com.example.jobworldserver.auth.jwt.JwtAuthenticationFilter;
import com.example.jobworldserver.auth.service.CustomOAuth2UserService;
import com.example.jobworldserver.auth.service.CustomUserDetailsService;
import com.example.jobworldserver.auth.service.JwtService;
import com.example.jobworldserver.oauth.handler.OAuth2FailureHandler;
import com.example.jobworldserver.oauth.handler.OAuth2SuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://13.125.112.86:8080}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 기본 로그인/로그아웃 페이지 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/job-world/login"))

                .authorizeHttpRequests(authz -> authz
                        // Swagger/OpenAPI 관련 경로 - Spring Boot 3.5 호환성 개선
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-config",
                                "/swagger-config/**"
                        ).permitAll()

                        // 정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 인증 없이 접근 가능한 경로들
                        .requestMatchers("/job-world/signup").permitAll()
                        .requestMatchers("/job-world/verify-email").permitAll()
                        .requestMatchers("/job-world/check-verification").permitAll()
                        .requestMatchers("/job-world/login").permitAll()
                        .requestMatchers("/job-world/login/oauth2/code/**").permitAll()

                        // 테스트 경로
                        .requestMatchers("/test/**").permitAll()

                        // 인증된 사용자만 접근 가능한 경로들
                        .requestMatchers("/job-world/**").authenticated()
                        .requestMatchers("/oauth2/user-info").authenticated()

                        // 역할 기반 접근 제어
                        .requestMatchers("/job-world/cards").hasAnyAuthority("TEACHER", "STUDENT", "NORMAL")
                        .requestMatchers("/api/teacher/**").hasAuthority("TEACHER")
                        .requestMatchers("/api/student/**").hasAuthority("STUDENT")
                        .requestMatchers("/api/normal/**").hasAuthority("NORMAL")

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                        .redirectionEndpoint(redir -> redir
                                .baseUri("/job-world/login/oauth2/code/*"))
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService, objectMapper) {
                    @Override
                    protected boolean shouldNotFilter(HttpServletRequest request) {
                        String path = request.getRequestURI();

                        // Swagger 관련 모든 경로는 JWT 필터링 제외
                        if (isSwaggerPath(path)) {
                            return true;
                        }

                        // 기존 인증 제외 경로들
                        return path.startsWith("/job-world/signup") ||
                                path.startsWith("/job-world/verify-email") ||
                                path.startsWith("/job-world/check-verification") ||
                                path.equals("/job-world/login") ||
                                path.startsWith("/job-world/login/oauth2/code/") ||
                                path.startsWith("/test/");
                    }

                    private boolean isSwaggerPath(String path) {
                        return path.equals("/swagger-ui.html") ||
                                path.startsWith("/swagger-ui/") ||
                                path.startsWith("/swagger-resources/") ||
                                path.startsWith("/webjars/") ||
                                path.equals("/v3/api-docs") ||
                                path.startsWith("/v3/api-docs/") ||
                                path.equals("/v3/api-docs.yaml") ||
                                path.startsWith("/swagger-config");
                    }
                }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 포트 번호를 포함한 허용 Origin 설정
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://13.125.112.86:8080",
                "https://13.125.112.86:8080"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}