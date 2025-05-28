package com.example.jobworldserver.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EnvironmentDebugConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentDebugConfig.class);

    private final Environment environment;

    public EnvironmentDebugConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logEnvironmentVariables() {
        logger.info("GOOGLE_CLIENT_ID (System.getenv): {}", System.getenv("GOOGLE_CLIENT_ID"));
        logger.info("GOOGLE_CLIENT_SECRET (System.getenv): {}", System.getenv("GOOGLE_CLIENT_SECRET"));
        logger.info("GOOGLE_CLIENT_ID (Spring Environment): {}", environment.getProperty("GOOGLE_CLIENT_ID"));
        logger.info("GOOGLE_CLIENT_SECRET (Spring Environment): {}", environment.getProperty("GOOGLE_CLIENT_SECRET"));
    }
}