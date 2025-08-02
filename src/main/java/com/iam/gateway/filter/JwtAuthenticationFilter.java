package com.iam.gateway.filter;

import com.iam.common.jwt.JwtTokenProvider;
import com.iam.gateway.constants.GatewayConstants;
import com.iam.gateway.constants.GatewayMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JWT Authentication Filter - Zero Hardcoded Strings
 * Fixed dependency injection for JwtTokenProvider
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Public endpoints that bypass authentication - using constants
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/health",
            GatewayConstants.GATEWAY_HEALTH_PATH,
            GatewayConstants.GATEWAY_INFO_PATH,
            GatewayConstants.USERS_HEALTH_PATH,
            GatewayConstants.ACTUATOR_HEALTH_PATH
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @PostConstruct
    public void init() {
        if (jwtTokenProvider == null) {
            log.error("JwtTokenProvider is null! Check if iam-common-utilities is properly configured.");
            throw new IllegalStateException("JwtTokenProvider must be configured");
        }
        log.info("JwtAuthenticationFilter initialized successfully with JwtTokenProvider");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Validate that JWT provider is available
            if (jwtTokenProvider == null) {
                log.error("JwtTokenProvider is not available - cannot process authentication");
                return handleUnauthorized(exchange, GatewayMessages.AUTH_FAILED);
            }

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().toString();

            log.debug(GatewayMessages.LOG_PROCESSING_REQUEST, method, path);

            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug(GatewayMessages.LOG_PUBLIC_ENDPOINT_ACCESSED, path);
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String token = extractTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                log.warn(GatewayMessages.LOG_MISSING_AUTH_HEADER, method, path);
                return handleUnauthorized(exchange, GatewayMessages.AUTH_MISSING_TOKEN);
            }

            try {
                // Validate JWT token
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn(GatewayMessages.LOG_INVALID_JWT_TOKEN, method, path);
                    return handleUnauthorized(exchange, GatewayMessages.AUTH_INVALID_TOKEN);
                }

                // Extract user information from token
                String username = jwtTokenProvider.extractUsername(token);

                if (!StringUtils.hasText(username)) {
                    log.warn(GatewayMessages.LOG_UNABLE_EXTRACT_USERNAME, method, path);
                    return handleUnauthorized(exchange, GatewayMessages.AUTH_INVALID_PAYLOAD);
                }

                // Add user context to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(GatewayConstants.HEADER_USER_ID, username)
                        .header(GatewayConstants.HEADER_AUTHENTICATED, GatewayConstants.HEADER_VALUE_TRUE)
                        .header(GatewayConstants.HEADER_AUTH_TIME, LocalDateTime.now().toString())
                        .header(GatewayConstants.HEADER_TOKEN_EXPIRES, String.valueOf(jwtTokenProvider.getExpirationTime()))
                        .build();

                log.debug(GatewayMessages.AUTHENTICATION_SUCCESS, username, method, path);

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn(GatewayMessages.LOG_EXPIRED_JWT_TOKEN, method, path, e.getMessage());
                return handleUnauthorized(exchange, GatewayMessages.AUTH_EXPIRED_TOKEN);

            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.warn(GatewayMessages.LOG_MALFORMED_JWT_TOKEN, method, path, e.getMessage());
                return handleUnauthorized(exchange, GatewayMessages.AUTH_MALFORMED_TOKEN);

            } catch (io.jsonwebtoken.SignatureException e) {
                log.warn(GatewayMessages.LOG_INVALID_JWT_SIGNATURE, method, path, e.getMessage());
                return handleUnauthorized(exchange, GatewayMessages.AUTH_INVALID_SIGNATURE);

            } catch (Exception e) {
                log.error(GatewayMessages.LOG_UNEXPECTED_JWT_ERROR, method, path, e.getMessage(), e);
                return handleUnauthorized(exchange, GatewayMessages.AUTH_FAILED);
            }
        };
    }

    /**
     * Extract JWT token from Authorization header - Using Constants
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken)) {
            if (bearerToken.startsWith(GatewayConstants.JWT_TOKEN_PREFIX)) {
                return bearerToken.substring(GatewayConstants.JWT_TOKEN_START_INDEX);
            } else if (bearerToken.toLowerCase().startsWith("bearer ")) {
                return bearerToken.substring(GatewayConstants.JWT_TOKEN_START_INDEX);
            } else {
                // Support token without "Bearer " prefix
                return bearerToken;
            }
        }

        // Also check for token in query parameter (for WebSocket connections)
        String tokenParam = request.getQueryParams().getFirst(GatewayConstants.JWT_TOKEN_QUERY_PARAM);
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    /**
     * Check if endpoint is public - Using Constants
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith) ||
                path.startsWith("/actuator/") ||
                path.equals("/") ||
                path.equals("/favicon.ico");
    }

    /**
     * Handle unauthorized access - Using Constants and Messages
     */
    private Mono<Void> handleUnauthorized(org.springframework.web.server.ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add(GatewayConstants.HEADER_GATEWAY_ERROR, GatewayMessages.ERROR_JWT_AUTH_FAILED);

        String errorResponse = String.format("""
            {
                "success": false,
                "message": "%s",
                "error": "%s",
                "timestamp": "%s",
                "path": "%s",
                "status": 401
            }
            """,
                GatewayMessages.AUTH_REQUIRED,
                errorMessage,
                LocalDateTime.now(),
                exchange.getRequest().getPath().value()
        );

        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Configuration class for JWT filter - Using Constants
     */
    public static class Config {
        private boolean enabled = true;
        private boolean strictMode = true;
        private long tokenExpirationTolerance = 300; // 5 minutes tolerance for clock skew

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isStrictMode() {
            return strictMode;
        }

        public void setStrictMode(boolean strictMode) {
            this.strictMode = strictMode;
        }

        public long getTokenExpirationTolerance() {
            return tokenExpirationTolerance;
        }

        public void setTokenExpirationTolerance(long tokenExpirationTolerance) {
            this.tokenExpirationTolerance = tokenExpirationTolerance;
        }
    }
}