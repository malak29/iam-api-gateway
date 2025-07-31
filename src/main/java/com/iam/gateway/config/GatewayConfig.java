package com.iam.gateway.config;

import com.iam.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Gateway Configuration with Production-Ready Service URLs
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Service URLs from environment configuration
    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${services.organization-service.url:http://localhost:8083}")
    private String organizationServiceUrl;

    @Value("${services.chat-service.url:http://localhost:8084}")
    private String chatServiceUrl;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        log.info("Configuring Gateway Routes with Service URLs:");
        log.info("User Service: {}", userServiceUrl);
        log.info("Auth Service: {}", authServiceUrl);
        log.info("Organization Service: {}", organizationServiceUrl);
        log.info("Chat Service: {}", chatServiceUrl);

        return builder.routes()
                // ===================================================================
                // USER SERVICE - Protected Routes with JWT Authentication
                // ===================================================================
                .route("user-service-protected", r -> r
                        .path("/api/v1/users/**")
                        .and()
                        .not(p -> p.path("/api/v1/users/health")) // Exclude health check
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "user-service")
                                .addResponseHeader("X-Gateway-Response", "user-service")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service")
                                )
                        )
                        .uri(userServiceUrl) // ✅ Dynamic URL
                )

                // User Service Health Check (Public)
                .route("user-service-health", r -> r
                        .path("/api/v1/users/health")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "user-service-health")
                        )
                        .uri(userServiceUrl) // ✅ Dynamic URL
                )

                // ===================================================================
                // AUTH SERVICE - Public Routes (Login, Register, etc.)
                // ===================================================================
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "auth-service")
                                .addResponseHeader("X-Gateway-Response", "auth-service")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()) // Rate limit by IP for auth
                                )
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth-service")
                                )
                        )
                        .uri(authServiceUrl) // ✅ Dynamic URL
                )

                // ===================================================================
                // ORGANIZATION SERVICE - Protected Routes
                // ===================================================================
                .route("organization-service", r -> r
                        .path("/api/v1/organizations/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "organization-service")
                                .addResponseHeader("X-Gateway-Response", "organization-service")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName("organization-service-cb")
                                        .setFallbackUri("forward:/fallback/organization-service")
                                )
                        )
                        .uri(organizationServiceUrl) // ✅ Dynamic URL
                )

                // ===================================================================
                // CHAT SERVICE - Protected Routes
                // ===================================================================
                .route("chat-service", r -> r
                        .path("/api/v1/chat/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "chat-service")
                                .addResponseHeader("X-Gateway-Response", "chat-service")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName("chat-service-cb")
                                        .setFallbackUri("forward:/fallback/chat-service")
                                )
                        )
                        .uri(chatServiceUrl) // ✅ Dynamic URL
                )

                // ===================================================================
                // ADMIN ROUTES - High Security
                // ===================================================================
                .route("admin-routes", r -> r
                        .path("/api/v1/admin/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "admin-service")
                                .addRequestHeader("X-Requires-Admin", "true")
                                .addResponseHeader("X-Gateway-Response", "admin-service")
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(adminRateLimiter()) // Stricter rate limiting
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri(userServiceUrl) // Admin routes go to user service for now
                )

                .build();
    }

    /**
     * Redis Template for Rate Limiting
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(new StringRedisSerializer())
                .value(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    /**
     * Standard Rate Limiter - 10 requests per second, burst of 20
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }

    /**
     * Admin Rate Limiter - Stricter: 5 requests per second, burst of 10
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter adminRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(5, 10, 1);
    }

    /**
     * User-based Rate Limiting Key Resolver
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return reactor.core.publisher.Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * IP-based Rate Limiting Key Resolver (for auth endpoints)
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return reactor.core.publisher.Mono.just(clientIp);
        };
    }
}