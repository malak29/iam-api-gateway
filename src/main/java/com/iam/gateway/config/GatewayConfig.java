package com.iam.gateway.config;

import com.iam.gateway.constants.GatewayConstants;
import com.iam.gateway.constants.GatewayMessages;
import com.iam.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Gateway Configuration - Zero Hardcoded Strings
 * All strings managed through constants and properties
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiGatewayProperties properties;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        log.info(GatewayMessages.LOG_CONFIGURING_ROUTES);
        log.info("User Service: {}", properties.getServices().getUserServiceUrl());
        log.info("Auth Service: {}", properties.getServices().getAuthServiceUrl());
        log.info("Organization Service: {}", properties.getServices().getOrganizationServiceUrl());
        log.info("Chat Service: {}", properties.getServices().getChatServiceUrl());

        return builder.routes()
                // User Service - Protected Routes with JWT Authentication
                .route(GatewayConstants.USER_SERVICE_PROTECTED_ROUTE, r -> r
                        .path(GatewayConstants.USERS_API_PATH)
                        .and()
                        .not(p -> p.path(GatewayConstants.USERS_HEALTH_PATH)) // Exclude health check
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.USER_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.USER_SERVICE)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.USER_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.USER_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getUserServiceUrl())
                )

                // User Service Health Check (Public)
                .route(GatewayConstants.USER_SERVICE_HEALTH_ROUTE, r -> r
                        .path(GatewayConstants.USERS_HEALTH_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.USER_SERVICE + "-health")
                        )
                        .uri(properties.getServices().getUserServiceUrl())
                )

                // Auth Service - Public Routes (Login, Register, etc.)
                .route(GatewayConstants.AUTH_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.AUTH_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.AUTH_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.AUTH_SERVICE)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()) // Rate limit by IP for auth
                                )
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.AUTH_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.AUTH_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getAuthServiceUrl())
                )

                // Organization Service - Protected Routes
                .route(GatewayConstants.ORGANIZATION_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.ORGANIZATIONS_API_PATH)
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.ORGANIZATION_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.ORGANIZATION_SERVICE)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.ORGANIZATION_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.ORGANIZATION_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getOrganizationServiceUrl())
                )

                // Chat Service - Protected Routes
                .route(GatewayConstants.CHAT_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.CHAT_API_PATH)
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.CHAT_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.CHAT_SERVICE)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.CHAT_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.CHAT_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getChatServiceUrl())
                )

                // Admin Routes - High Security
                .route(GatewayConstants.ADMIN_ROUTES, r -> r
                        .path(GatewayConstants.ADMIN_API_PATH)
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.ADMIN_SERVICE)
                                .addRequestHeader(GatewayConstants.HEADER_REQUIRES_ADMIN, GatewayConstants.HEADER_VALUE_TRUE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.ADMIN_SERVICE)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(adminRateLimiter()) // Stricter rate limiting
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri(properties.getServices().getUserServiceUrl()) // Admin routes go to user service for now
                )

                .build();
    }

    // Note: Using Spring Boot's auto-configured ReactiveRedisTemplate
    // No need to create our own - Spring Boot provides one automatically

    /**
     * Standard Rate Limiter - Using Properties (PRIMARY for Gateway auto-config)
     */
    @Bean(GatewayConstants.BEAN_REDIS_RATE_LIMITER)
    @Primary
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(
                properties.getRateLimit().getReplenishRate(),
                properties.getRateLimit().getBurstCapacity(),
                properties.getRateLimit().getRequestedTokens()
        );
    }

    /**
     * Admin Rate Limiter - Stricter for admin operations
     */
    @Bean(GatewayConstants.BEAN_ADMIN_RATE_LIMITER)
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter adminRateLimiter() {
        // Admin rate limiting is 50% of normal rate
        int adminReplenishRate = properties.getRateLimit().getReplenishRate() / 2;
        int adminBurstCapacity = properties.getRateLimit().getBurstCapacity() / 2;

        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(
                adminReplenishRate,
                adminBurstCapacity,
                properties.getRateLimit().getRequestedTokens()
        );
    }

    /**
     * User-based Rate Limiting Key Resolver - Using Constants
     */
    @Bean(GatewayConstants.BEAN_USER_KEY_RESOLVER)
    @Primary
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst(GatewayConstants.HEADER_USER_ID);
            return reactor.core.publisher.Mono.just(userId != null ? userId : GatewayConstants.RATE_LIMIT_KEY_ANONYMOUS);
        };
    }

    /**
     * IP-based Rate Limiting Key Resolver - Using Constants
     */
    @Bean(GatewayConstants.BEAN_IP_KEY_RESOLVER)
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                    GatewayConstants.RATE_LIMIT_KEY_UNKNOWN;
            return reactor.core.publisher.Mono.just(clientIp);
        };
    }
}