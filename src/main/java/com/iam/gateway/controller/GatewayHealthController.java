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
 * Gateway Health Controller - Zero Hardcoded Strings
 */
@RestController
@RequestMapping(GatewayConstants.GATEWAY_API_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class GatewayHealthController {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final WebClient.Builder webClientBuilder;
    private final ApiGatewayProperties properties;

    @GetMapping(GatewayConstants.HEALTH_ENDPOINT)
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> health() {
        log.info(GatewayMessages.LOG_GATEWAY_HEALTH_REQUESTED);

        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put(GatewayConstants.GATEWAY_KEY, GatewayConstants.STATUS_UP);
        healthStatus.put(GatewayConstants.APPLICATION_KEY, GatewayConstants.APPLICATION_NAME);
        healthStatus.put(GatewayConstants.PORT_KEY, GatewayConstants.DEFAULT_PORT);
        healthStatus.put(GatewayConstants.TIMESTAMP_KEY, LocalDateTime.now());

        return checkAllServices()
                .map(services -> {
                    healthStatus.put(GatewayConstants.SERVICES_KEY, services);
                    healthStatus.put(GatewayConstants.SERVICE_URLS_KEY, properties.getServiceUrlsMap());

                    String overallStatus = calculateOverallStatus(services);
                    healthStatus.put(GatewayConstants.OVERALL_STATUS_KEY, overallStatus);
                    healthStatus.put(GatewayConstants.HEALTHY_SERVICES_KEY, countHealthyServices(services));
                    healthStatus.put(GatewayConstants.TOTAL_SERVICES_KEY, services.size());

                    ApiResponse<Map<String, Object>> response = ApiResponse.success(
                            healthStatus,
                            String.format(GatewayMessages.HEALTH_CHECK_COMPLETED, overallStatus)
                    );

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    log.error(GatewayMessages.LOG_HEALTH_CHECK_ERROR, error.toString(), error);

                    healthStatus.put(GatewayConstants.SERVICES_KEY, GatewayMessages.HEALTH_CHECK_ERROR_SERVICES);
                    healthStatus.put(GatewayConstants.ERROR_KEY, error.toString());
                    healthStatus.put(GatewayConstants.OVERALL_STATUS_KEY, GatewayConstants.STATUS_CRITICAL);

                    ApiResponse<Map<String, Object>> response = ApiResponse.success(
                            healthStatus,
                            GatewayMessages.HEALTH_CHECK_ERROR
                    );

                    return Mono.just(ResponseEntity.status(503).body(response));
                });
    }

    @GetMapping(GatewayConstants.INFO_ENDPOINT)
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put(GatewayConstants.SERVICE_KEY, GatewayConstants.APPLICATION_NAME);
        info.put(GatewayConstants.VERSION_KEY, GatewayConstants.APPLICATION_VERSION);
        info.put(GatewayConstants.DESCRIPTION_KEY, GatewayConstants.APPLICATION_DESCRIPTION);
        info.put(GatewayConstants.PORT_KEY, GatewayConstants.DEFAULT_PORT);

        // Service routing information using constants
        Map<String, String> routes = new HashMap<>();
        routes.put(GatewayConstants.USERS_API_PATH,
                String.format(GatewayMessages.SERVICE_INFO_FORMAT, GatewayConstants.USER_SERVICE, properties.getServices().getUserServiceUrl()));
        routes.put(GatewayConstants.AUTH_API_PATH,
                String.format(GatewayMessages.SERVICE_INFO_FORMAT, GatewayConstants.AUTH_SERVICE, properties.getServices().getAuthServiceUrl()));
        routes.put(GatewayConstants.ORGANIZATIONS_API_PATH,
                String.format(GatewayMessages.SERVICE_INFO_FORMAT, GatewayConstants.ORGANIZATION_SERVICE, properties.getServices().getOrganizationServiceUrl()));
        routes.put(GatewayConstants.CHAT_API_PATH,
                String.format(GatewayMessages.SERVICE_INFO_FORMAT, GatewayConstants.CHAT_SERVICE, properties.getServices().getChatServiceUrl()));
        routes.put(GatewayConstants.ADMIN_API_PATH, GatewayMessages.ADMIN_SERVICE_INFO);

        info.put(GatewayConstants.ROUTES_KEY, routes);

        // Features using message constants
        info.put(GatewayConstants.FEATURES_KEY, Map.of(
                GatewayConstants.JWT_AUTH_FEATURE, GatewayMessages.FEATURE_JWT_AUTH,
                GatewayConstants.RATE_LIMITING_FEATURE, GatewayMessages.FEATURE_RATE_LIMITING,
                GatewayConstants.CIRCUIT_BREAKERS_FEATURE, GatewayMessages.FEATURE_CIRCUIT_BREAKERS,
                GatewayConstants.CORS_SUPPORT_FEATURE, GatewayMessages.FEATURE_CORS_SUPPORT,
                GatewayConstants.HEALTH_AGGREGATION_FEATURE, GatewayMessages.FEATURE_HEALTH_AGGREGATION,
                GatewayConstants.REQUEST_LOGGING_FEATURE, GatewayMessages.FEATURE_REQUEST_LOGGING
        ));

        info.put(GatewayConstants.TIMESTAMP_KEY, LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(info, GatewayMessages.GATEWAY_INFO_SUCCESS));
    }

    @GetMapping(GatewayConstants.METRICS_ENDPOINT)
    public ResponseEntity<ApiResponse<Map<String, Object>>> metrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Placeholder for actual metrics (would be collected from actuator)
        metrics.put(GatewayConstants.REQUESTS_TOTAL_KEY, GatewayMessages.TRACKED_IN_ACTUATOR);
        metrics.put(GatewayConstants.ACTIVE_CONNECTIONS_KEY, GatewayMessages.TRACKED_IN_ACTUATOR);
        metrics.put(GatewayConstants.RESPONSE_TIMES_KEY, GatewayMessages.TRACKED_IN_ACTUATOR);
        metrics.put(GatewayConstants.CIRCUIT_BREAKER_STATES_KEY, GatewayMessages.TRACKED_IN_ACTUATOR);

        metrics.put(GatewayConstants.SERVICE_URLS_KEY, properties.getServiceUrlsMap());
        metrics.put(GatewayConstants.TIMESTAMP_KEY, LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(metrics, GatewayMessages.METRICS_SUCCESS));
    }

    /**
     * Check all downstream services - Including Redis
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
                .doOnError(error -> log.warn(GatewayMessages.USER_SERVICE_HEALTH_FAILED, error.toString()));

        // Check Auth Service
        Mono<String> authServiceHealth = webClient.get()
                .uri(properties.getServices().getAuthServiceUrl() + GatewayConstants.AUTH_HEALTH_PATH)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> GatewayConstants.STATUS_UP)
                .onErrorReturn(GatewayConstants.STATUS_DOWN)
                .timeout(Duration.ofSeconds(GatewayConstants.HEALTH_CHECK_TIMEOUT))
                .doOnError(error -> log.warn(GatewayMessages.AUTH_SERVICE_HEALTH_FAILED, error.toString()));

        // Check Redis
        Mono<String> redisHealth = redisTemplate.opsForValue()
                .set(GatewayConstants.REDIS_HEALTH_CHECK_KEY, GatewayConstants.REDIS_HEALTH_CHECK_VALUE)
                .then(redisTemplate.opsForValue().get(GatewayConstants.REDIS_HEALTH_CHECK_KEY))
                .map(value -> GatewayConstants.REDIS_HEALTH_CHECK_VALUE.equals(value) ?
                        GatewayConstants.STATUS_UP : GatewayConstants.STATUS_DOWN)
                .onErrorReturn(GatewayConstants.STATUS_DOWN)
                .timeout(Duration.ofSeconds(GatewayConstants.REDIS_TIMEOUT))
                .doOnError(error -> log.warn(GatewayMessages.REDIS_HEALTH_FAILED, error.toString()));

        // Future services - using constants
        Mono<String> organizationServiceHealth = Mono.just(GatewayConstants.STATUS_NOT_IMPLEMENTED);
        Mono<String> chatServiceHealth = Mono.just(GatewayConstants.STATUS_NOT_IMPLEMENTED);

        return Mono.zip(userServiceHealth, authServiceHealth, redisHealth, organizationServiceHealth, chatServiceHealth)
                .map(tuple -> {
                    Map<String, String> services = new HashMap<>();
                    services.put(GatewayConstants.USER_SERVICE, tuple.getT1());
                    services.put(GatewayConstants.AUTH_SERVICE, tuple.getT2());
                    services.put(GatewayConstants.REDIS_SERVICE, tuple.getT3());
                    services.put(GatewayConstants.ORGANIZATION_SERVICE, tuple.getT4());
                    services.put(GatewayConstants.CHAT_SERVICE, tuple.getT5());
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
        } else if (upServices >= 3) { // At least user, auth, and redis services
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