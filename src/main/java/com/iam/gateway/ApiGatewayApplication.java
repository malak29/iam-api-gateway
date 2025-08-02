package com.iam.gateway;

import com.iam.gateway.config.ApiGatewayProperties;
import com.iam.gateway.constants.GatewayConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;

/**
 * API Gateway Application - Zero Hardcoded Strings
 * All constants managed centrally for maintainability
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.iam.gateway",     // Gateway components
        "com.iam.common"       // Common utilities (including JwtTokenProvider)
})
@EnableConfigurationProperties(ApiGatewayProperties.class)
@RequiredArgsConstructor
@Slf4j
public class ApiGatewayApplication {

    private final ApiGatewayProperties properties;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("Starting {} version {}", GatewayConstants.APPLICATION_NAME, GatewayConstants.APPLICATION_VERSION);
        log.info("Port: {}", GatewayConstants.DEFAULT_PORT);
        log.info("Component scanning: com.iam.gateway, com.iam.common");
    }

    /**
     * Route Configuration - Using Constants for All Strings
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring routes with service URLs from properties");

        return builder.routes()
                // User Service - Protected Routes
                .route(GatewayConstants.USER_SERVICE_PROTECTED_ROUTE, r -> r
                        .path(GatewayConstants.USERS_API_PATH)
                        .and()
                        .not(p -> p.path(GatewayConstants.USERS_HEALTH_PATH))
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.USER_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.USER_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_VERSION, GatewayConstants.APPLICATION_VERSION)
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.USER_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.USER_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getUserServiceUrl())
                )

                // User Service Health Check
                .route(GatewayConstants.USER_SERVICE_HEALTH_ROUTE, r -> r
                        .path(GatewayConstants.USERS_HEALTH_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.USER_SERVICE + "-health")
                        )
                        .uri(properties.getServices().getUserServiceUrl())
                )

                // Auth Service Routes
                .route(GatewayConstants.AUTH_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.AUTH_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.AUTH_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.AUTH_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_VERSION, GatewayConstants.APPLICATION_VERSION)
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.AUTH_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.AUTH_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getAuthServiceUrl())
                )

                // Organization Service Routes
                .route(GatewayConstants.ORGANIZATION_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.ORGANIZATIONS_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.ORGANIZATION_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.ORGANIZATION_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_VERSION, GatewayConstants.APPLICATION_VERSION)
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.ORGANIZATION_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.ORGANIZATION_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getOrganizationServiceUrl())
                )

                // Chat Service Routes
                .route(GatewayConstants.CHAT_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.CHAT_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.CHAT_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.CHAT_SERVICE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_VERSION, GatewayConstants.APPLICATION_VERSION)
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.CHAT_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.CHAT_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getChatServiceUrl())
                )

                // Admin Routes
                .route(GatewayConstants.ADMIN_ROUTES, r -> r
                        .path(GatewayConstants.ADMIN_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addRequestHeader(GatewayConstants.HEADER_SERVICE_ROUTE, GatewayConstants.ADMIN_SERVICE)
                                .addRequestHeader(GatewayConstants.HEADER_REQUIRES_ADMIN, GatewayConstants.HEADER_VALUE_TRUE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.ADMIN_SERVICE)
                        )
                        .uri(properties.getServices().getUserServiceUrl()) // Admin routes to user service for now
                )

                .build();
    }

    /**
     * CORS Configuration - Using Constants for Headers
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(properties.getCors().isAllowCredentials());

        // Handle origins from centralized config
        if (properties.isPermissiveCors()) {
            corsConfig.addAllowedOriginPattern(GatewayConstants.DEFAULT_CORS_ORIGINS);
            log.info("CORS configured for development (permissive mode)");
        } else {
            for (String origin : properties.getAllowedOriginsArray()) {
                corsConfig.addAllowedOrigin(origin.trim());
            }
            log.info("CORS configured for production with specific origins");
        }

        // Use constants for headers and methods
        for (String header : properties.getCors().getAllowedHeaders()) {
            corsConfig.addAllowedHeader(header);
        }

        for (String method : properties.getCors().getAllowedMethods()) {
            corsConfig.addAllowedMethod(method);
        }

        for (String header : properties.getCors().getExposedHeaders()) {
            corsConfig.addExposedHeader(header);
        }

        corsConfig.setMaxAge(properties.getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}