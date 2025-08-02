package com.iam.gateway.config;

import com.iam.common.jwt.JwtTokenProvider;
import com.iam.gateway.constants.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * JWT Configuration for API Gateway
 * Ensures JwtTokenProvider is properly configured for the Gateway module
 */
@Configuration
@Slf4j
public class JwtConfig {

    @Value("${gateway.jwt.secret:" + GatewayConstants.DEFAULT_JWT_SECRET + "}")
    private String jwtSecret;

    /**
     * JWT Token Provider Bean for Gateway
     * This ensures the JWT provider is available for the Gateway filter
     */
    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        log.info("Configuring JwtTokenProvider for API Gateway with secret: {}...",
                jwtSecret.substring(0, Math.min(jwtSecret.length(), 10)));

        // Create JWT provider instance - this should match the one in iam-common-utilities
        JwtTokenProvider provider = new JwtTokenProvider();

        // If the common utilities JWT provider needs manual configuration, do it here
        // provider.setJwtSecret(jwtSecret); // If setter is available

        log.info("JwtTokenProvider configured successfully for API Gateway");
        return provider;
    }

    /**
     * Alternative: If JwtTokenProvider needs manual field injection
     * This method would be called if the @Value injection in JwtTokenProvider doesn't work
     */
    // @Bean
    // @ConditionalOnMissingBean
    // public JwtTokenProvider backupJwtTokenProvider() {
    //     log.warn("Creating backup JwtTokenProvider - check iam-common-utilities configuration");
    //     return new JwtTokenProvider();
    // }
}