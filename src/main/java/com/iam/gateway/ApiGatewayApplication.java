package com.iam.gateway;

import com.iam.gateway.config.ApiGatewayProperties;
import com.iam.gateway.constants.GatewayConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.iam.gateway",
        "com.iam.common"
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