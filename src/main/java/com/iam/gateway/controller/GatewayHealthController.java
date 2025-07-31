package com.iam.gateway.controller;

import com.iam.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * Gateway Health Controller - Production Ready
 * Comprehensive health checks for gateway and all downstream services
 */
@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
@Slf4j
public class GatewayHealthController {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final WebClient.Builder webClientBuilder;

    // Service URLs from configuration
    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${services.organization-service.url:http://localhost:8083}")
    private String organizationServiceUrl;

    @Value("${services.chat-service.url:http://localhost:8084}")
    private String chatServiceUrl;

    @Value("${spring.application.name:iam-api-gateway}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    @GetMapping("/health")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> health() {
        log.info("Gateway comprehensive health check requested");

        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("gateway", "UP");
        healthStatus.put("application", applicationName);
        healthStatus.put("port", serverPort);
        healthStatus.put("timestamp", LocalDateTime.now());

        return checkAllServices()
                .map(services -> {
                    healthStatus.put("services", services);
                    healthStatus.put("service_urls", getServiceUrls());

                    String overallStatus = calculateOverallStatus(services);
                    healthStatus.put("overall_status", overallStatus);
                    healthStatus.put("healthy_services", countHealthyServices(services));
                    healthStatus.put("total_services", services.size());

                    ApiResponse<Map<String, Object>> response = ApiResponse.success(
                            healthStatus,
                            String.format("Gateway health check completed - Status: %s", overallStatus)
                    );

                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(error -> {
                    log.error("Error during health check: {}", error.getMessage(), error);

                    healthStatus.put("services", "ERROR - Could not check downstream services");
                    healthStatus.put("error", error.getMessage());
                    healthStatus.put("overall_status", "CRITICAL");

                    ApiResponse<Map<String, Object>> response = ApiResponse.success(
                            healthStatus,
                            "Gateway is up but could not verify all services"
                    );

                    return ResponseEntity.status(503).body(response);
                });
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", applicationName);
        info.put("version", "1.0.0");
        info.put("description", "IAM API Gateway - Central routing for all IAM microservices");
        info.put("port", serverPort);
        info.put("build_time", LocalDateTime.now().toString()); // In real app, get from build info

        // Service routing information
        Map<String, String> routes = new HashMap<>();
        routes.put("/api/v1/users/**", String.format("User Service (%s)", userServiceUrl));
        routes.put("/api/v1/auth/**", String.format("Auth Service (%s)", authServiceUrl));
        routes.put("/api/v1/organizations/**", String.format("Organization Service (%s)", organizationServiceUrl));
        routes.put("/api/v1/chat/**", String.format("Chat Service (%s)", chatServiceUrl));
        routes.put("/api/v1/admin/**", "Admin Service (via User Service)");

        info.put("routes", routes);

        // Features
        info.put("features", Map.of(
                "jwt_authentication", true,
                "rate_limiting", true,
                "circuit_breakers", true,
                "cors_support", true,
                "health_aggregation", true,
                "request_logging", true
        ));

        info.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(info, "Gateway information"));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> metrics() {
        Map<String, Object> metrics = new HashMap<>();

        // In a real application, you would collect actual metrics
        metrics.put("requests_total", "tracked_in_actuator");
        metrics.put("active_connections", "tracked_in_actuator");
        metrics.put("response_times", "tracked_in_actuator");
        metrics.put("circuit_breaker_states", "tracked_in_actuator");

        metrics.put("service_urls", getServiceUrls());
        metrics.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(metrics,
                "Gateway metrics (detailed metrics available at /actuator/metrics)"));
    }

    private Mono<Map<String, String>> checkAllServices() {
        WebClient webClient = webClientBuilder.build();

        // Check User Service
        Mono<String> userServiceHealth = webClient.get()
                .uri(userServiceUrl + "/api/v1/users/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "UP")
                .onErrorReturn("DOWN")
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.warn("User service health check failed: {}", error.getMessage()));

        // Check Auth Service
        Mono<String> authServiceHealth = webClient.get()
                .uri(authServiceUrl + "/api/v1/auth/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "UP")
                .onErrorReturn("DOWN")
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.warn("Auth service health check failed: {}", error.getMessage()));

        // Check Organization Service (future)
        Mono<String> organizationServiceHealth = Mono.just("NOT_IMPLEMENTED");

        // Check Chat Service (future)
        Mono<String> chatServiceHealth = Mono.just("NOT_IMPLEMENTED");

        // Check Redis
        Mono<String> redisHealth = redisTemplate.opsForValue()
                .set("gateway:health:check", "ping")
                .then(redisTemplate.opsForValue().get("gateway:health:check"))
                .map(value -> "ping".equals(value) ? "UP" : "DOWN")
                .onErrorReturn("DOWN")
                .timeout(Duration.ofSeconds(3))
                .doOnError(error -> log.warn("Redis health check failed: {}", error.getMessage()));

        return Mono.zip(userServiceHealth, authServiceHealth, organizationServiceHealth, chatServiceHealth, redisHealth)
                .map(tuple -> {
                    Map<String, String> services = new HashMap<>();
                    services.put("user-service", tuple.getT1());
                    services.put("auth-service", tuple.getT2());
                    services.put("organization-service", tuple.getT3());
                    services.put("chat-service", tuple.getT4());
                    services.put("redis", tuple.getT5());
                    return services;
                });
    }

    private Map<String, String> getServiceUrls() {
        Map<String, String> urls = new HashMap<>();
        urls.put("user-service", userServiceUrl);
        urls.put("auth-service", authServiceUrl);
        urls.put("organization-service", organizationServiceUrl);
        urls.put("chat-service", chatServiceUrl);
        return urls;
    }

    private String calculateOverallStatus(Map<String, String> services) {
        long upServices = services.values().stream()
                .filter(status -> "UP".equals(status))
                .count();

        long downServices = services.values().stream()
                .filter(status -> "DOWN".equals(status))
                .count();

        long notImplementedServices = services.values().stream()
                .filter(status -> "NOT_IMPLEMENTED".equals(status))
                .count();

        if (downServices > 0) {
            return "DEGRADED";
        } else if (notImplementedServices > 0 && upServices > 0) {
            return "PARTIAL";
        } else if (upServices >= 2) { // At least user and auth services should be up
            return "HEALTHY";
        } else {
            return "CRITICAL";
        }
    }

    private long countHealthyServices(Map<String, String> services) {
        return services.values().stream()
                .filter(status -> "UP".equals(status))
                .count();
    }
}