package com.iam.gateway.controller;

import com.iam.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback Controller - Production Ready
 * Handles circuit breaker fallbacks when downstream services are unavailable
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @Value("${spring.application.name:iam-api-gateway}")
    private String applicationName;

    @Value("${fallback.retry-after-seconds:60}")
    private int retryAfterSeconds;

    @GetMapping("/user-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> userServiceFallback() {
        log.warn("User service circuit breaker activated - service is unavailable");

        Map<String, Object> fallbackData = Map.of(
                "service", "user-service",
                "status", "UNAVAILABLE",
                "fallback_triggered", true,
                "retry_after_seconds", retryAfterSeconds,
                "alternative_action", "Please try again later or contact support if the issue persists"
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("User service is temporarily unavailable")
                .error("SERVICE_UNAVAILABLE")
                .data(fallbackData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-Fallback-Reason", "CIRCUIT_BREAKER_OPEN")
                .body(response);
    }

    @GetMapping("/auth-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> authServiceFallback() {
        log.warn("Auth service circuit breaker activated - authentication is unavailable");

        Map<String, Object> fallbackData = Map.of(
                "service", "auth-service",
                "status", "UNAVAILABLE",
                "fallback_triggered", true,
                "retry_after_seconds", retryAfterSeconds,
                "alternative_action", "Authentication is temporarily disabled. Cached tokens may still work.",
                "impact", "New logins and token refreshes are unavailable"
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Authentication service is temporarily unavailable")
                .error("AUTH_SERVICE_UNAVAILABLE")
                .data(fallbackData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-Fallback-Reason", "CIRCUIT_BREAKER_OPEN")
                .body(response);
    }

    @GetMapping("/organization-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> organizationServiceFallback() {
        log.warn("Organization service circuit breaker activated - service is unavailable");

        Map<String, Object> fallbackData = Map.of(
                "service", "organization-service",
                "status", "UNAVAILABLE",
                "fallback_triggered", true,
                "retry_after_seconds", retryAfterSeconds,
                "alternative_action", "Organization management is temporarily unavailable",
                "cached_data_available", false
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Organization service is temporarily unavailable")
                .error("ORGANIZATION_SERVICE_UNAVAILABLE")
                .data(fallbackData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-Fallback-Reason", "CIRCUIT_BREAKER_OPEN")
                .body(response);
    }

    @GetMapping("/chat-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatServiceFallback() {
        log.warn("Chat service circuit breaker activated - service is unavailable");

        Map<String, Object> fallbackData = Map.of(
                "service", "chat-service",
                "status", "UNAVAILABLE",
                "fallback_triggered", true,
                "retry_after_seconds", retryAfterSeconds,
                "alternative_action", "Real-time messaging is temporarily unavailable",
                "websocket_connections", "DISABLED"
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Chat service is temporarily unavailable")
                .error("CHAT_SERVICE_UNAVAILABLE")
                .data(fallbackData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-Fallback-Reason", "CIRCUIT_BREAKER_OPEN")
                .body(response);
    }

    @GetMapping("/admin-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminServiceFallback() {
        log.warn("Admin service circuit breaker activated - administrative functions unavailable");

        Map<String, Object> fallbackData = Map.of(
                "service", "admin-service",
                "status", "UNAVAILABLE",
                "fallback_triggered", true,
                "retry_after_seconds", retryAfterSeconds,
                "alternative_action", "Administrative functions are temporarily unavailable",
                "impact", "User management and system configuration disabled"
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Administrative service is temporarily unavailable")
                .error("ADMIN_SERVICE_UNAVAILABLE")
                .data(fallbackData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-Fallback-Reason", "CIRCUIT_BREAKER_OPEN")
                .body(response);
    }

    /**
     * Generic fallback for any undefined service
     */
    @GetMapping("/**")
    public ResponseEntity<ApiResponse<Map<String, Object>>> genericFallback() {
        log.warn("Generic circuit breaker activated - unknown service is unavailable");

        Map<String, Object> fallbackData = Map.of(
                "service", "unknown",
                "status", "UNAVAILABLE",
                "fallback_triggered", true,
                "retry_after_seconds", retryAfterSeconds,
                "alternative_action", "The requested service is temporarily unavailable"
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Service temporarily unavailable")
                .error("SERVICE_UNAVAILABLE")
                .data(fallbackData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-Fallback-Reason", "CIRCUIT_BREAKER_OPEN")
                .body(response);
    }
}