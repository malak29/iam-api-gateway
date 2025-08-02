package com.iam.gateway.controller;

import com.iam.common.response.ApiResponse;
import com.iam.gateway.config.ApiGatewayProperties;
import com.iam.gateway.constants.GatewayConstants;
import com.iam.gateway.constants.GatewayMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Health Controller - Using Constants and Correct ApiResponse
 */
@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
@Slf4j
public class GatewayHealthController {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final WebClient.Builder webClientBuilder;
    private final ApiGatewayProperties properties;

    @GetMapping("/health")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> health() {
        log.info(GatewayMessages.LOG_GATEWAY_HEALTH_REQUESTED);

        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("gateway", GatewayConstants.STATUS_UP);
        healthStatus.put("application", GatewayConstants.APPLICATION_NAME);
        healthStatus.put("port", GatewayConstants.DEFAULT_PORT);
        healthStatus.put("timestamp", LocalDateTime.now());

        return checkAllServices()
                .map(services -> {
                    healthStatus.put("services", services);
                    healthStatus.put("service_urls", properties.getServiceUrlsMap());

                    String overallStatus = calculateOverallStatus(services);
                    healthStatus.put("overall_status", overallStatus);
                    healthStatus.put("healthy_services", countHealthyServices(services));
                    healthStatus.put("total_services", services.size());

                    // Using correct ApiResponse factory method
                    ApiResponse<Map<String, Object>> response = ApiResponse.success(
                            healthStatus,
                            String.format(GatewayMessages.HEALTH_CHECK_COMPLETED, overallStatus)
                    );

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    log.error("Error during health check: {}", error.toString(), error);

                    healthStatus.put("services", "ERROR - Could not check downstream services");
                    healthStatus.put("error", error.toString());
                    healthStatus.put("overall_status", GatewayConstants.STATUS_CRITICAL);

                    ApiResponse<Map<String, Object>> response = ApiResponse.success(
                            healthStatus,
                            GatewayMessages.HEALTH_CHECK_ERROR
                    );

                    return Mono.just(ResponseEntity.status(503).body(response));
                });
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", GatewayConstants.APPLICATION_NAME);
        info.put("version", GatewayConstants.APPLICATION_VERSION);
        info.put("description", GatewayConstants.APPLICATION_DESCRIPTION);
        info.put("port", GatewayConstants.DEFAULT_PORT);

        // Service routing information using constants

        Map<String, String> routes = new HashMap<>();
        routes.put(GatewayConstants.USERS_API_PATH,
                String.format("User Service (%s)", properties.getServices().getUserServiceUrl()));
        routes.put(GatewayConstants.AUTH_API_PATH,
                String.format("Auth Service (%s)", properties.getServices().getAuthServiceUrl()));
        routes.put(GatewayConstants.ORGANIZATIONS_API_PATH,
                String.format("Organization Service (%s)", properties.getServices().getOrganizationServiceUrl()));
        routes.put(GatewayConstants.CHAT_API_PATH,
                String.format("Chat Service (%s)", properties.getServices().getChatServiceUrl()));
        routes.put(GatewayConstants.ADMIN_API_PATH, "Admin Service (via User Service)");

        info.put("routes", routes);

        // Features using message constants
        info.put("features", Map.of(
                "jwt_authentication", GatewayMessages.FEATURE_JWT_AUTH,
                "rate_limiting", GatewayMessages.FEATURE_RATE_LIMITING,
                "circuit_breakers", GatewayMessages.FEATURE_CIRCUIT_BREAKERS,
                "cors_support", GatewayMessages.FEATURE_CORS_SUPPORT,
                "health_aggregation", GatewayMessages.FEATURE_HEALTH_AGGREGATION,
                "request_logging", GatewayMessages.FEATURE_REQUEST_LOGGING
        ));

        info.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(info, GatewayMessages.GATEWAY_INFO_SUCCESS));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> metrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Placeholder for actual metrics (would be collected from actuator)
        metrics.put("requests_total", "tracked_in_actuator");
        metrics.put("active_connections", "tracked_in_actuator");
        metrics.put("response_times", "tracked_in_actuator");
        metrics.put("circuit_breaker_states", "tracked_in_actuator");

        metrics.put("service_urls", properties.getServiceUrlsMap());
        metrics.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(metrics, GatewayMessages.METRICS_SUCCESS));
    }

    /**
     * Check all downstream services - Using Constants
     */
    private Mono<Map<String, String>> checkAllServices() {
        WebClient webClient = webClientBuilder.build();

        // Check User Service
        Mono<String> userServiceHealth = webClient.get()
                .uri(properties.getServices().getUserServiceUrl() + GatewayConstants.USERS_HEALTH_PATH)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> GatewayConstants.STATUS_UP)
                .onErrorReturn(GatewayConstants.STATUS_DOWN)
                .timeout(Duration.ofSeconds(GatewayConstants.HEALTH_CHECK_TIMEOUT))
                .doOnError(error -> log.warn("User service health check failed: {}", error.toString()));

        // Check Auth Service
        Mono<String> authServiceHealth = webClient.get()
                .uri(properties.getServices().getAuthServiceUrl() + "/api/v1/auth/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> GatewayConstants.STATUS_UP)
                .onErrorReturn(GatewayConstants.STATUS_DOWN)
                .timeout(Duration.ofSeconds(GatewayConstants.HEALTH_CHECK_TIMEOUT))
                .doOnError(error -> log.warn("Auth service health check failed: {}", error.toString()));

        // Future services - using constants
        Mono<String> organizationServiceHealth = Mono.just(GatewayConstants.STATUS_NOT_IMPLEMENTED);
        Mono<String> chatServiceHealth = Mono.just(GatewayConstants.STATUS_NOT_IMPLEMENTED);

        // Check Redis
        Mono<String> redisHealth = redisTemplate.opsForValue()
                .set(GatewayConstants.REDIS_HEALTH_CHECK_KEY, GatewayConstants.REDIS_HEALTH_CHECK_VALUE)
                .then(redisTemplate.opsForValue().get(GatewayConstants.REDIS_HEALTH_CHECK_KEY))
                .map(value -> GatewayConstants.REDIS_HEALTH_CHECK_VALUE.equals(value) ?
                        GatewayConstants.STATUS_UP : GatewayConstants.STATUS_DOWN)
                .onErrorReturn(GatewayConstants.STATUS_DOWN)
                .timeout(Duration.ofSeconds(GatewayConstants.REDIS_TIMEOUT))
                .doOnError(error -> log.warn("Redis health check failed: {}", error.toString()));

        return Mono.zip(userServiceHealth, authServiceHealth, organizationServiceHealth, chatServiceHealth, redisHealth)
                .map(tuple -> {
                    Map<String, String> services = new HashMap<>();
                    services.put(GatewayConstants.USER_SERVICE, tuple.getT1());
                    services.put(GatewayConstants.AUTH_SERVICE, tuple.getT2());
                    services.put(GatewayConstants.ORGANIZATION_SERVICE, tuple.getT3());
                    services.put(GatewayConstants.CHAT_SERVICE, tuple.getT4());
                    services.put("redis", tuple.getT5());
                    return services;
                });
    }

    /**
     * Calculate overall system status - Using Constants
     */
    private String calculateOverallStatus(Map<String, String> services) {
        long upServices = services.values().stream()
                .filter(GatewayConstants.STATUS_UP::equals)
                .count();

        long downServices = services.values().stream()
                .filter(GatewayConstants.STATUS_DOWN::equals)
                .count();

        long notImplementedServices = services.values().stream()
                .filter(GatewayConstants.STATUS_NOT_IMPLEMENTED::equals)
                .count();

        if (downServices > 0) {
            return GatewayConstants.STATUS_DEGRADED;
        } else if (notImplementedServices > 0 && upServices > 0) {
            return GatewayConstants.STATUS_PARTIAL;
        } else if (upServices >= 2) { // At least user and auth services
            return GatewayConstants.STATUS_HEALTHY;
        } else {
            return GatewayConstants.STATUS_CRITICAL;
        }
    }

    /**
     * Count healthy services - Using Constants
     */
    private long countHealthyServices(Map<String, String> services) {
        return services.values().stream()
                .filter(GatewayConstants.STATUS_UP::equals)
                .count();
    }
}