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
     * Simple Route Configuration - Basic routing without filters
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring routes with service URLs from properties");

        return builder.routes()
                // User Service Routes
                .route(GatewayConstants.USER_SERVICE_PROTECTED_ROUTE, r -> r
                        .path(GatewayConstants.USERS_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.USER_SERVICE)
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.USER_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.USER_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getUserServiceUrl())
                )

                // Auth Service Routes
                .route(GatewayConstants.AUTH_SERVICE_ROUTE, r -> r
                        .path(GatewayConstants.AUTH_API_PATH)
                        .filters(f -> f
                                .addRequestHeader(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                                .addResponseHeader(GatewayConstants.HEADER_GATEWAY_RESPONSE, GatewayConstants.AUTH_SERVICE)
                                .circuitBreaker(config -> config
                                        .setName(GatewayConstants.AUTH_SERVICE_CIRCUIT_BREAKER)
                                        .setFallbackUri(GatewayConstants.AUTH_SERVICE_FALLBACK)
                                )
                        )
                        .uri(properties.getServices().getAuthServiceUrl())
                )

                .build();
    }

    /**
     * CORS Configuration - Simplified
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addExposedHeader("*");
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        log.info("CORS configured for development (permissive mode)");
        return new CorsWebFilter(source);
    }
}