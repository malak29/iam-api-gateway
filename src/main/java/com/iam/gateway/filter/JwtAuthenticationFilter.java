package com.iam.gateway.filter;

import com.iam.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * JWT Authentication Filter - Production Ready
 * Validates JWT tokens and injects user context for downstream services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    // Public endpoints that bypass authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/health",
            "/api/v1/gateway/health",
            "/api/v1/gateway/info",
            "/api/v1/users/health",
            "/actuator/health"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().toString();

            log.debug("Processing request: {} {}", method, path);

            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint accessed: {}", path);
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String token = extractTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                log.warn("Missing Authorization header for protected endpoint: {} {}", method, path);
                return handleUnauthorized(exchange, "Missing or invalid Authorization token");
            }

            try {
                // Validate JWT token
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("Invalid JWT token for endpoint: {} {}", method, path);
                    return handleUnauthorized(exchange, "Invalid or expired JWT token");
                }

                // Extract user information from token
                String username = jwtTokenProvider.extractUsername(token);

                if (!StringUtils.hasText(username)) {
                    log.warn("Unable to extract username from JWT token for endpoint: {} {}", method, path);
                    return handleUnauthorized(exchange, "Invalid token payload");
                }

                // Add user context to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", username)
                        .header("X-Authenticated", "true")
                        .header("X-Auth-Time", LocalDateTime.now().toString())
                        .header("X-Token-Expires", String.valueOf(jwtTokenProvider.getExpirationTime()))
                        .build();

                log.debug("Successfully authenticated user: {} for endpoint: {} {}", username, method, path);

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("Expired JWT token for endpoint: {} {}, error: {}", method, path, e.getMessage());
                return handleUnauthorized(exchange, "JWT token has expired");

            } catch (io.jsonwebtoken.MalformedJwtException e) {
                log.warn("Malformed JWT token for endpoint: {} {}, error: {}", method, path, e.getMessage());
                return handleUnauthorized(exchange, "Malformed JWT token");

            } catch (io.jsonwebtoken.SignatureException e) {
                log.warn("Invalid JWT signature for endpoint: {} {}, error: {}", method, path, e.getMessage());
                return handleUnauthorized(exchange, "Invalid JWT signature");

            } catch (Exception e) {
                log.error("Unexpected error validating JWT token for endpoint: {} {}, error: {}", method, path, e.getMessage(), e);
                return handleUnauthorized(exchange, "Authentication failed");
            }
        };
    }

    /**
     * Extract JWT token from Authorization header
     * Supports both "Bearer token" and "token" formats
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken)) {
            if (bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            } else if (bearerToken.startsWith("bearer ")) {
                return bearerToken.substring(7);
            } else {
                // Support token without "Bearer " prefix
                return bearerToken;
            }
        }

        // Also check for token in query parameter (for WebSocket connections)
        String tokenParam = request.getQueryParams().getFirst("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith) ||
                path.startsWith("/actuator/") ||
                path.equals("/") ||
                path.equals("/favicon.ico");
    }

    /**
     * Handle unauthorized access with detailed error response
     */
    private Mono<Void> handleUnauthorized(org.springframework.web.server.ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-Gateway-Error", "JWT_AUTHENTICATION_FAILED");

        String errorResponse = String.format("""
            {
                "success": false,
                "message": "Authentication required",
                "error": "%s",
                "timestamp": "%s",
                "path": "%s",
                "status": 401
            }
            """,
                errorMessage,
                LocalDateTime.now(),
                exchange.getRequest().getPath().value()
        );

        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Configuration class for the JWT filter
     */
    public static class Config {
        private boolean enabled = true;
        private boolean strictMode = true; // Strict JWT validation
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