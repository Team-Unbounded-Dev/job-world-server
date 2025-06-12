package com.example.jobworldserver.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.timeout:60000}")
    private long timeout;

    private ClientResources clientResources;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Redis 연결 설정 시작: {}:{}", host, port);

        // Redis 서버 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);

        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
            log.info("Redis 비밀번호 설정됨");
        }

        // 클라이언트 리소스 설정
        clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();

        // 소켓 옵션 설정
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(timeout))
                .keepAlive(true)
                .tcpNoDelay(true)
                .build();

        // 클라이언트 옵션 설정
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        // Lettuce 클라이언트 설정
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofSeconds(5))
                .shutdownTimeout(Duration.ofMillis(100))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(true);
        factory.afterPropertiesSet();

        // 연결 테스트
        try {
            factory.getConnection().ping();
            log.info("Redis 연결 성공: {}:{}", host, port);
        } catch (Exception e) {
            log.error("Redis 연결 실패: {}:{}, 오류: {}", host, port, e.getMessage());
            throw new RuntimeException("Redis 연결 실패", e);
        }

        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key는 String, Value는 JSON으로 직렬화
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        log.info("RedisTemplate 설정 완료");
        return template;
    }

    @PreDestroy
    public void cleanup() {
        if (clientResources != null) {
            clientResources.shutdown();
            log.info("Redis 클라이언트 리소스 정리 완료");
        }
    }
}