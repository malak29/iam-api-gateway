package com.iam.gateway.controller;

import com.iam.common.response.ApiResponse;
import com.iam.gateway.config.ApiGatewayProperties;
import com.iam.gateway.constants.GatewayConstants;
import com.iam.gateway.constants.GatewayMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback Controller - Using Correct ApiResponse Factory Methods
 * Compatible with iam-common-utilities ApiResponse class
 */
@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
@Slf4j
public class FallbackController {

    private final ApiGatewayProperties properties;

    @GetMapping("/user-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> userServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.USER_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                "service", GatewayConstants.USER_SERVICE,
                "status", GatewayConstants.STATUS_UNAVAILABLE,
                "fallback_triggered", true,
                "retry_after_seconds", properties.getFallback().getRetryAfterSeconds(),
                "service_url", properties.getServices().getUserServiceUrl(),
                "alternative_action", GatewayMessages.ACTION_RETRY_LATER
        );

        // Using your ApiResponse static factory method correctly
        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.USER_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping("/auth-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> authServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.AUTH_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                "service", GatewayConstants.AUTH_SERVICE,
                "status", GatewayConstants.STATUS_UNAVAILABLE,
                "fallback_triggered", true,
                "retry_after_seconds", properties.getFallback().getRetryAfterSeconds(),
                "service_url", properties.getServices().getAuthServiceUrl(),
                "alternative_action", GatewayMessages.ACTION_AUTH_DISABLED,
                "impact", GatewayMessages.IMPACT_AUTH_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.AUTH_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping("/organization-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> organizationServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.ORGANIZATION_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                "service", GatewayConstants.ORGANIZATION_SERVICE,
                "status", GatewayConstants.STATUS_UNAVAILABLE,
                "fallback_triggered", true,
                "retry_after_seconds", properties.getFallback().getRetryAfterSeconds(),
                "service_url", properties.getServices().getOrganizationServiceUrl(),
                "alternative_action", GatewayMessages.ACTION_ORG_UNAVAILABLE,
                "cached_data_available", false
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.ORGANIZATION_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping("/chat-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.CHAT_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                "service", GatewayConstants.CHAT_SERVICE,
                "status", GatewayConstants.STATUS_UNAVAILABLE,
                "fallback_triggered", true,
                "retry_after_seconds", properties.getFallback().getRetryAfterSeconds(),
                "service_url", properties.getServices().getChatServiceUrl(),
                "alternative_action", GatewayMessages.ACTION_CHAT_UNAVAILABLE,
                "impact", GatewayMessages.IMPACT_CHAT_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.CHAT_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping("/admin-service")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.ADMIN_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                "service", GatewayConstants.ADMIN_SERVICE,
                "status", GatewayConstants.STATUS_UNAVAILABLE,
                "fallback_triggered", true,
                "retry_after_seconds", properties.getFallback().getRetryAfterSeconds(),
                "service_url", properties.getServices().getUserServiceUrl(), // Admin goes through user service
                "alternative_action", GatewayMessages.ACTION_ADMIN_UNAVAILABLE,
                "impact", GatewayMessages.IMPACT_ADMIN_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.ADMIN_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
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
                "status", GatewayConstants.STATUS_UNAVAILABLE,
                "fallback_triggered", true,
                "retry_after_seconds", properties.getFallback().getRetryAfterSeconds(),
                "alternative_action", GatewayMessages.ACTION_UNKNOWN_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.GENERIC_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }
}