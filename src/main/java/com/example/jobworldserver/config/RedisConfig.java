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

    // password 필드 없애거나 그냥 둬도 상관없지만, 설정에서 없으면 빈 문자열로 처리됨
    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.timeout:60000}")
    private long timeout;

    private ClientResources clientResources;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Redis 연결 설정 시작: {}:{}", host, port);

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);

        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
            log.info("Redis 비밀번호 설정됨");
        } else {
            log.info("Redis 비밀번호 설정 안함");
        }

        clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(timeout))
                .keepAlive(true)
                .tcpNoDelay(true)
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

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
