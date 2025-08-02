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
 * Fallback Controller - Zero Hardcoded Strings
 * All strings managed through constants for maintainability
 */
@RestController
@RequestMapping(GatewayConstants.FALLBACK_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class FallbackController {

    private final ApiGatewayProperties properties;

    @GetMapping(GatewayConstants.USER_SERVICE_FALLBACK_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> userServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.USER_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                GatewayConstants.SERVICE_KEY, GatewayConstants.USER_SERVICE,
                GatewayConstants.STATUS_KEY, GatewayConstants.STATUS_UNAVAILABLE,
                GatewayConstants.FALLBACK_TRIGGERED_KEY, true,
                GatewayConstants.RETRY_AFTER_SECONDS_KEY, properties.getFallback().getRetryAfterSeconds(),
                GatewayConstants.SERVICE_URL_KEY, properties.getServices().getUserServiceUrl(),
                GatewayConstants.ALTERNATIVE_ACTION_KEY, GatewayMessages.ACTION_RETRY_LATER
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.USER_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping(GatewayConstants.AUTH_SERVICE_FALLBACK_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> authServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.AUTH_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                GatewayConstants.SERVICE_KEY, GatewayConstants.AUTH_SERVICE,
                GatewayConstants.STATUS_KEY, GatewayConstants.STATUS_UNAVAILABLE,
                GatewayConstants.FALLBACK_TRIGGERED_KEY, true,
                GatewayConstants.RETRY_AFTER_SECONDS_KEY, properties.getFallback().getRetryAfterSeconds(),
                GatewayConstants.SERVICE_URL_KEY, properties.getServices().getAuthServiceUrl(),
                GatewayConstants.ALTERNATIVE_ACTION_KEY, GatewayMessages.ACTION_AUTH_DISABLED,
                GatewayConstants.IMPACT_KEY, GatewayMessages.IMPACT_AUTH_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.AUTH_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping(GatewayConstants.ORGANIZATION_SERVICE_FALLBACK_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> organizationServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.ORGANIZATION_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                GatewayConstants.SERVICE_KEY, GatewayConstants.ORGANIZATION_SERVICE,
                GatewayConstants.STATUS_KEY, GatewayConstants.STATUS_UNAVAILABLE,
                GatewayConstants.FALLBACK_TRIGGERED_KEY, true,
                GatewayConstants.RETRY_AFTER_SECONDS_KEY, properties.getFallback().getRetryAfterSeconds(),
                GatewayConstants.SERVICE_URL_KEY, properties.getServices().getOrganizationServiceUrl(),
                GatewayConstants.ALTERNATIVE_ACTION_KEY, GatewayMessages.ACTION_ORG_UNAVAILABLE,
                GatewayConstants.CACHED_DATA_AVAILABLE_KEY, false
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.ORGANIZATION_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping(GatewayConstants.CHAT_SERVICE_FALLBACK_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> chatServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.CHAT_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                GatewayConstants.SERVICE_KEY, GatewayConstants.CHAT_SERVICE,
                GatewayConstants.STATUS_KEY, GatewayConstants.STATUS_UNAVAILABLE,
                GatewayConstants.FALLBACK_TRIGGERED_KEY, true,
                GatewayConstants.RETRY_AFTER_SECONDS_KEY, properties.getFallback().getRetryAfterSeconds(),
                GatewayConstants.SERVICE_URL_KEY, properties.getServices().getChatServiceUrl(),
                GatewayConstants.ALTERNATIVE_ACTION_KEY, GatewayMessages.ACTION_CHAT_UNAVAILABLE,
                GatewayConstants.IMPACT_KEY, GatewayMessages.IMPACT_CHAT_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.CHAT_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }

    @GetMapping(GatewayConstants.ADMIN_SERVICE_FALLBACK_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminServiceFallback() {
        log.warn(GatewayMessages.LOG_CIRCUIT_BREAKER_ACTIVATED, GatewayConstants.ADMIN_SERVICE);

        Map<String, Object> fallbackData = Map.of(
                GatewayConstants.SERVICE_KEY, GatewayConstants.ADMIN_SERVICE,
                GatewayConstants.STATUS_KEY, GatewayConstants.STATUS_UNAVAILABLE,
                GatewayConstants.FALLBACK_TRIGGERED_KEY, true,
                GatewayConstants.RETRY_AFTER_SECONDS_KEY, properties.getFallback().getRetryAfterSeconds(),
                GatewayConstants.SERVICE_URL_KEY, properties.getServices().getUserServiceUrl(), // Admin goes through user service
                GatewayConstants.ALTERNATIVE_ACTION_KEY, GatewayMessages.ACTION_ADMIN_UNAVAILABLE,
                GatewayConstants.IMPACT_KEY, GatewayMessages.IMPACT_ADMIN_SERVICE
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
    @GetMapping(GatewayConstants.GENERIC_FALLBACK_PATH)
    public ResponseEntity<ApiResponse<Map<String, Object>>> genericFallback() {
        log.warn(GatewayMessages.LOG_GENERIC_FALLBACK_ACTIVATED);

        Map<String, Object> fallbackData = Map.of(
                GatewayConstants.SERVICE_KEY, GatewayConstants.UNKNOWN_SERVICE,
                GatewayConstants.STATUS_KEY, GatewayConstants.STATUS_UNAVAILABLE,
                GatewayConstants.FALLBACK_TRIGGERED_KEY, true,
                GatewayConstants.RETRY_AFTER_SECONDS_KEY, properties.getFallback().getRetryAfterSeconds(),
                GatewayConstants.ALTERNATIVE_ACTION_KEY, GatewayMessages.ACTION_UNKNOWN_SERVICE
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success(fallbackData, GatewayMessages.GENERIC_SERVICE_UNAVAILABLE);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(GatewayConstants.HEADER_RETRY_AFTER, String.valueOf(properties.getFallback().getRetryAfterSeconds()))
                .header(GatewayConstants.HEADER_FALLBACK_REASON, GatewayConstants.HEADER_VALUE_CIRCUIT_BREAKER_OPEN)
                .body(response);
    }
}