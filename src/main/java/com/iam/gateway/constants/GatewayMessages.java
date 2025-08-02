package com.iam.gateway.constants;

/**
 * Message Constants - All user-facing messages and error codes
 */
public final class GatewayMessages {

    private GatewayMessages() {} // Prevent instantiation

    // ===================================================================
    // SUCCESS MESSAGES
    // ===================================================================
    public static final String HEALTH_CHECK_COMPLETED = "Gateway health check completed - Status: %s";
    public static final String GATEWAY_INFO_SUCCESS = "Gateway information";
    public static final String METRICS_SUCCESS = "Gateway metrics (detailed metrics available at /actuator/metrics)";
    public static final String AUTHENTICATION_SUCCESS = "Successfully authenticated user: %s for endpoint: %s %s";

    // ===================================================================
    // ERROR MESSAGES - Authentication
    // ===================================================================
    public static final String AUTH_REQUIRED = "Authentication required";
    public static final String AUTH_MISSING_TOKEN = "Missing or invalid Authorization token";
    public static final String AUTH_INVALID_TOKEN = "Invalid or expired JWT token";
    public static final String AUTH_EXPIRED_TOKEN = "JWT token has expired";
    public static final String AUTH_MALFORMED_TOKEN = "Malformed JWT token";
    public static final String AUTH_INVALID_SIGNATURE = "Invalid JWT signature";
    public static final String AUTH_FAILED = "Authentication failed";
    public static final String AUTH_INVALID_PAYLOAD = "Invalid token payload";

    // ===================================================================
    // ERROR MESSAGES - Service Unavailable
    // ===================================================================
    public static final String USER_SERVICE_UNAVAILABLE = "User service is temporarily unavailable";
    public static final String AUTH_SERVICE_UNAVAILABLE = "Authentication service is temporarily unavailable";
    public static final String ORGANIZATION_SERVICE_UNAVAILABLE = "Organization service is temporarily unavailable";
    public static final String CHAT_SERVICE_UNAVAILABLE = "Chat service is temporarily unavailable";
    public static final String ADMIN_SERVICE_UNAVAILABLE = "Administrative service is temporarily unavailable";
    public static final String GENERIC_SERVICE_UNAVAILABLE = "Service temporarily unavailable";

    // ===================================================================
    // ERROR MESSAGES - Health Checks
    // ===================================================================
    public static final String HEALTH_CHECK_ERROR = "Gateway is up but could not verify all services";
    public static final String USER_SERVICE_HEALTH_FAILED = "User service health check failed: %s";
    public static final String AUTH_SERVICE_HEALTH_FAILED = "Auth service health check failed: %s";
    public static final String REDIS_HEALTH_FAILED = "Redis health check failed: %s";

    // ===================================================================
    // ERROR CODES
    // ===================================================================
    public static final String ERROR_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    public static final String ERROR_USER_SERVICE_UNAVAILABLE = "USER_SERVICE_UNAVAILABLE";
    public static final String ERROR_AUTH_SERVICE_UNAVAILABLE = "AUTH_SERVICE_UNAVAILABLE";
    public static final String ERROR_ORGANIZATION_SERVICE_UNAVAILABLE = "ORGANIZATION_SERVICE_UNAVAILABLE";
    public static final String ERROR_CHAT_SERVICE_UNAVAILABLE = "CHAT_SERVICE_UNAVAILABLE";
    public static final String ERROR_ADMIN_SERVICE_UNAVAILABLE = "ADMIN_SERVICE_UNAVAILABLE";
    public static final String ERROR_JWT_AUTH_FAILED = "JWT_AUTHENTICATION_FAILED";

    // ===================================================================
    // LOG MESSAGES - Info
    // ===================================================================
    public static final String LOG_GATEWAY_HEALTH_REQUESTED = "Gateway comprehensive health check requested";
    public static final String LOG_PUBLIC_ENDPOINT_ACCESSED = "Public endpoint accessed: %s";
    public static final String LOG_CONFIGURING_ROUTES = "Configuring Gateway Routes with Service URLs:";
    public static final String LOG_SERVICE_URL = "%s Service: %s";
    public static final String LOG_PROCESSING_REQUEST = "Processing request: %s %s";

    // ===================================================================
    // LOG MESSAGES - Warnings
    // ===================================================================
    public static final String LOG_MISSING_AUTH_HEADER = "Missing Authorization header for protected endpoint: %s %s";
    public static final String LOG_INVALID_JWT_TOKEN = "Invalid JWT token for endpoint: %s %s";
    public static final String LOG_EXPIRED_JWT_TOKEN = "Expired JWT token for endpoint: %s %s, error: %s";
    public static final String LOG_MALFORMED_JWT_TOKEN = "Malformed JWT token for endpoint: %s %s, error: %s";
    public static final String LOG_INVALID_JWT_SIGNATURE = "Invalid JWT signature for endpoint: %s %s, error: %s";
    public static final String LOG_UNABLE_EXTRACT_USERNAME = "Unable to extract username from JWT token for endpoint: %s %s";
    public static final String LOG_CIRCUIT_BREAKER_ACTIVATED = "%s circuit breaker activated - service is unavailable";

    // ===================================================================
    // LOG MESSAGES - Errors
    // ===================================================================
    public static final String LOG_UNEXPECTED_JWT_ERROR = "Unexpected error validating JWT token for endpoint: %s %s, error: %s";
    public static final String LOG_HEALTH_CHECK_ERROR = "Error during health check: %s";
    public static final String LOG_WEBCLIENT_ERROR = "Gateway WebClient error response: %s %s from URL: %s";
    public static final String LOG_DOWNSTREAM_SERVICE_ERROR = "Downstream service error: %s - This may trigger circuit breaker";

    // ===================================================================
    // FEATURE DESCRIPTIONS
    // ===================================================================
    public static final String FEATURE_JWT_AUTH = "JWT Authentication with automatic token validation";
    public static final String FEATURE_RATE_LIMITING = "Redis-based rate limiting per user/IP";
    public static final String FEATURE_CIRCUIT_BREAKERS = "Circuit breakers for service resilience";
    public static final String FEATURE_CORS_SUPPORT = "Environment-specific CORS configuration";
    public static final String FEATURE_HEALTH_AGGREGATION = "Health check aggregation across all services";
    public static final String FEATURE_REQUEST_LOGGING = "Request/response logging and tracing";

    // ===================================================================
    // ALTERNATIVE ACTIONS (for fallbacks)
    // ===================================================================
    public static final String ACTION_RETRY_LATER = "Please try again later or contact support if the issue persists";
    public static final String ACTION_AUTH_DISABLED = "Authentication is temporarily disabled. Cached tokens may still work.";
    public static final String ACTION_ORG_UNAVAILABLE = "Organization management is temporarily unavailable";
    public static final String ACTION_CHAT_UNAVAILABLE = "Real-time messaging is temporarily unavailable";
    public static final String ACTION_ADMIN_UNAVAILABLE = "Administrative functions are temporarily unavailable";
    public static final String ACTION_UNKNOWN_SERVICE = "The requested service is temporarily unavailable";

    // ===================================================================
    // IMPACT DESCRIPTIONS
    // ===================================================================
    public static final String IMPACT_AUTH_SERVICE = "New logins and token refreshes are unavailable";
    public static final String IMPACT_ADMIN_SERVICE = "User management and system configuration disabled";
    public static final String IMPACT_CHAT_SERVICE = "WebSocket connections are disabled";

    // ===================================================================
    // REQUEST TRACKING
    // ===================================================================
    public static final String REQUEST_ID_PREFIX = "gw-";
    public static final String REQUEST_ID_SEPARATOR = "-";

    // ===================================================================
    // BEAN NAMES - WebClient Related
    // ===================================================================
    public static final String BEAN_HEALTH_CHECK_WEBCLIENT = "healthCheckWebClient";
    // Add these to your GatewayMessages class:

    public static final String HEALTH_CHECK_ERROR_SERVICES = "ERROR - Could not check downstream services";
    public static final String SERVICE_INFO_FORMAT = "%s Service (%s)";
    public static final String ADMIN_SERVICE_INFO = "Admin Service (via User Service)";
    public static final String TRACKED_IN_ACTUATOR = "tracked_in_actuator";
    public static final String LOG_GENERIC_FALLBACK_ACTIVATED = "Generic circuit breaker activated - unknown service is unavailable";
}
