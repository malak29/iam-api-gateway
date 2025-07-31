package com.iam.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * API Gateway Application - Production Ready
 * Central entry point for all IAM microservices with environment-specific configuration
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.iam.gateway", "com.iam.common"})
public class ApiGatewayApplication {

    // ===================================================================
    // SERVICE URLs - Environment Specific Configuration
    // ===================================================================

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${services.organization-service.url:http://localhost:8083}")
    private String organizationServiceUrl;

    @Value("${services.chat-service.url:http://localhost:8084}")
    private String chatServiceUrl;

    // CORS Configuration
    @Value("${cors.allowed-origins:*}")
    private String[] allowedOrigins;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Custom Route Locator - Production Ready with Dynamic URLs
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===================================================================
                // USER SERVICE ROUTES
                // ===================================================================
                .route("user-service-protected", r -> r
                        .path("/api/v1/users/**")
                        .and()
                        .not(p -> p.path("/api/v1/users/health")) // Exclude health check from auth
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "user-service")
                                .addResponseHeader("X-Gateway-Response", "user-service")
                                .addResponseHeader("X-Gateway-Version", "1.0.0")
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service")
                                )
                        )
                        .uri(userServiceUrl) // ✅ DYNAMIC - Production Ready!
                )

                // User Service Health Check (public endpoint)
                .route("user-service-health", r -> r
                        .path("/api/v1/users/health")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "user-service-health")
                        )
                        .uri(userServiceUrl) // ✅ DYNAMIC
                )

                // ===================================================================
                // AUTH SERVICE ROUTES (Mostly Public)
                // ===================================================================
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "auth-service")
                                .addResponseHeader("X-Gateway-Response", "auth-service")
                                .addResponseHeader("X-Gateway-Version", "1.0.0")
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth-service")
                                )
                        )
                        .uri(authServiceUrl) // ✅ DYNAMIC - Works in all environments!
                )

                // ===================================================================
                // GATEWAY HEALTH CHECK (Self-referencing)
                // ===================================================================
                .route("gateway-health", r -> r
                        .path("/api/v1/gateway/health", "/api/v1/gateway/info")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addResponseHeader("X-Gateway-Response", "gateway-self")
                        )
                        .uri("http://localhost:8080") // Self-reference for health aggregation
                )

                // ===================================================================
                // FUTURE SERVICES (Ready for expansion)
                // ===================================================================
                .route("organization-service", r -> r
                        .path("/api/v1/organizations/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "organization-service")
                                .addResponseHeader("X-Gateway-Response", "organization-service")
                                .addResponseHeader("X-Gateway-Version", "1.0.0")
                                .circuitBreaker(config -> config
                                        .setName("organization-service-cb")
                                        .setFallbackUri("forward:/fallback/organization-service")
                                )
                        )
                        .uri(organizationServiceUrl) // ✅ Future-ready
                )

                .route("chat-service", r -> r
                        .path("/api/v1/chat/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "chat-service")
                                .addResponseHeader("X-Gateway-Response", "chat-service")
                                .addResponseHeader("X-Gateway-Version", "1.0.0")
                                .circuitBreaker(config -> config
                                        .setName("chat-service-cb")
                                        .setFallbackUri("forward:/fallback/chat-service")
                                )
                        )
                        .uri(chatServiceUrl) // ✅ Environment configurable
                )

                // ===================================================================
                // ADMIN/MANAGEMENT ROUTES (Future)
                // ===================================================================
                .route("admin-routes", r -> r
                        .path("/api/v1/admin/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request", "true")
                                .addRequestHeader("X-Service-Route", "admin-service")
                                .addResponseHeader("X-Gateway-Response", "admin-service")
                                .addResponseHeader("X-Requires-Admin", "true")
                        )
                        .uri(userServiceUrl) // Admin routes can go to user service for now
                )

                .build();
    }

    /**
     * CORS Configuration - Environment Specific
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);

        // Environment-specific origins
        if (allowedOrigins.length == 1 && "*".equals(allowedOrigins[0])) {
            corsConfig.addAllowedOriginPattern("*"); // Development
        } else {
            for (String origin : allowedOrigins) {
                corsConfig.addAllowedOrigin(origin); // Production - specific origins
            }
        }

        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("X-Gateway-Response");
        corsConfig.addExposedHeader("X-Gateway-Version");
        corsConfig.addExposedHeader("X-Service-Route");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}