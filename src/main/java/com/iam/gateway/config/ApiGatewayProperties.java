package com.iam.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

/**
 * Centralized Configuration Properties for API Gateway
 * Eliminates duplication of @Value annotations across classes
 */
@Data
@ConfigurationProperties(prefix = "gateway")
public class ApiGatewayProperties {

    private Services services = new Services();
    private Cors cors = new Cors();
    private RateLimit rateLimit = new RateLimit();
    private Jwt jwt = new Jwt();
    private Fallback fallback = new Fallback();
    private WebClient webClient = new WebClient();

    @Data
    public static class Services {
        private String userServiceUrl = "http://localhost:8081";
        private String authServiceUrl = "http://localhost:8082";
        private String organizationServiceUrl = "http://localhost:8083";
        private String chatServiceUrl = "http://localhost:8084";
        private Duration defaultTimeout = Duration.ofSeconds(30);
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "*";
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
        private String[] allowedHeaders = {"*"};
        private String[] exposedHeaders = {"Authorization", "X-Gateway-Response", "X-Gateway-Version", "X-Service-Route"};
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }

    @Data
    public static class RateLimit {
        private int replenishRate = 10;
        private int burstCapacity = 20;
        private int requestedTokens = 1;
    }

    @Data
    public static class Jwt {
        private String secret = "dev-secret-key";
        private long expiration = 86400000; // 24 hours
        private long refreshExpiration = 604800000; // 7 days
    }

    @Data
    public static class Fallback {
        private int retryAfterSeconds = 60;
        private String defaultMessage = "Service temporarily unavailable";
    }

    @Data
    public static class WebClient {
        private int connectTimeoutMs = 10000;
        private int responseTimeoutSeconds = 30;
        private int readTimeoutSeconds = 30;
        private int writeTimeoutSeconds = 30;
        private int maxInMemorySize = 1048576;
    }

    // ===================================================================
    // CONVENIENCE METHODS
    // ===================================================================

    /**
     * Get allowed origins as array (split comma-separated string)
     */
    public String[] getAllowedOriginsArray() {
        if ("*".equals(cors.allowedOrigins)) {
            return new String[]{"*"};
        }
        return cors.allowedOrigins.split(",");
    }

    /**
     * Check if CORS is permissive (wildcard)
     */
    public boolean isPermissiveCors() {
        return "*".equals(cors.allowedOrigins);
    }

    /**
     * Get all service URLs as a map
     */
    public java.util.Map<String, String> getServiceUrlsMap() {
        return java.util.Map.of(
                "user-service", services.userServiceUrl,
                "auth-service", services.authServiceUrl,
                "organization-service", services.organizationServiceUrl,
                "chat-service", services.chatServiceUrl
        );
    }
}