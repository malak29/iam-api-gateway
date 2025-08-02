package com.iam.gateway.config;

import com.iam.common.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * JWT Configuration for API Gateway
 * Ensures JwtTokenProvider from common utilities is properly available
 */
@Configuration
@Slf4j
public class JwtConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostConstruct
    public void init() {
        if (jwtTokenProvider != null) {
            log.info("JwtTokenProvider from iam-common-utilities is available and configured");
        } else {
            log.error("JwtTokenProvider from iam-common-utilities is NOT available!");
            throw new IllegalStateException("JwtTokenProvider must be available from iam-common-utilities");
        }
    }
}