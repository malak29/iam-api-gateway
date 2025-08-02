package com.iam.gateway.constants;

/**
 * Application Constants - Single Source of Truth for all strings
 */
public final class GatewayConstants {

    private GatewayConstants() {} // Prevent instantiation

    // ===================================================================
    // APPLICATION INFO
    // ===================================================================
    public static final String APPLICATION_NAME = "iam-api-gateway";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final String APPLICATION_DESCRIPTION = "IAM API Gateway - Central routing for all IAM microservices";
    public static final int DEFAULT_PORT = 8080;
    public static final int MANAGEMENT_PORT = 8081;
    public static final int DEBUG_PORT = 5005;

    // ===================================================================
    // SERVICE NAMES
    // ===================================================================
    public static final String USER_SERVICE = "user-service";
    public static final String AUTH_SERVICE = "auth-service";
    public static final String ORGANIZATION_SERVICE = "organization-service";
    public static final String CHAT_SERVICE = "chat-service";
    public static final String ADMIN_SERVICE = "admin-service";
    public static final String GATEWAY_SERVICE = "gateway-self";

    // ===================================================================
    // ROUTE NAMES (for RouteLocator)
    // ===================================================================
    public static final String USER_SERVICE_PROTECTED_ROUTE = "user-service-protected";
    public static final String USER_SERVICE_HEALTH_ROUTE = "user-service-health";
    public static final String AUTH_SERVICE_ROUTE = "auth-service";
    public static final String ORGANIZATION_SERVICE_ROUTE = "organization-service";
    public static final String CHAT_SERVICE_ROUTE = "chat-service";
    public static final String ADMIN_ROUTES = "admin-routes";
    public static final String GATEWAY_HEALTH_ROUTE = "gateway-health";

    // ===================================================================
    // API PATHS
    // ===================================================================
    public static final String USERS_API_PATH = "/api/v1/users/**";
    public static final String USERS_HEALTH_PATH = "/api/v1/users/health";
    public static final String AUTH_API_PATH = "/api/v1/auth/**";
    public static final String ORGANIZATIONS_API_PATH = "/api/v1/organizations/**";
    public static final String CHAT_API_PATH = "/api/v1/chat/**";
    public static final String ADMIN_API_PATH = "/api/v1/admin/**";
    public static final String GATEWAY_HEALTH_PATH = "/api/v1/gateway/health";
    public static final String GATEWAY_INFO_PATH = "/api/v1/gateway/info";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";

    // ===================================================================
    // CIRCUIT BREAKER NAMES
    // ===================================================================
    public static final String USER_SERVICE_CIRCUIT_BREAKER = "user-service-cb";
    public static final String AUTH_SERVICE_CIRCUIT_BREAKER = "auth-service-cb";
    public static final String ORGANIZATION_SERVICE_CIRCUIT_BREAKER = "organization-service-cb";
    public static final String CHAT_SERVICE_CIRCUIT_BREAKER = "chat-service-cb";

    // ===================================================================
    // FALLBACK URIS
    // ===================================================================
    public static final String USER_SERVICE_FALLBACK = "forward:/fallback/user-service";
    public static final String AUTH_SERVICE_FALLBACK = "forward:/fallback/auth-service";
    public static final String ORGANIZATION_SERVICE_FALLBACK = "forward:/fallback/organization-service";
    public static final String CHAT_SERVICE_FALLBACK = "forward:/fallback/chat-service";

    // ===================================================================
    // HTTP HEADERS
    // ===================================================================
    public static final String HEADER_GATEWAY_REQUEST = "X-Gateway-Request";
    public static final String HEADER_SERVICE_ROUTE = "X-Service-Route";
    public static final String HEADER_GATEWAY_RESPONSE = "X-Gateway-Response";
    public static final String HEADER_GATEWAY_VERSION = "X-Gateway-Version";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_AUTHENTICATED = "X-Authenticated";
    public static final String HEADER_AUTH_TIME = "X-Auth-Time";
    public static final String HEADER_TOKEN_EXPIRES = "X-Token-Expires";
    public static final String HEADER_REQUIRES_ADMIN = "X-Requires-Admin";
    public static final String HEADER_FALLBACK_REASON = "X-Fallback-Reason";
    public static final String HEADER_GATEWAY_ERROR = "X-Gateway-Error";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_RETRY_AFTER = "Retry-After";

    // Standard HTTP headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";

    // ===================================================================
    // HTTP HEADER VALUES
    // ===================================================================
    public static final String HEADER_VALUE_TRUE = "true";
    public static final String HEADER_VALUE_BEARER_PREFIX = "Bearer ";
    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    public static final String HEADER_VALUE_CIRCUIT_BREAKER_OPEN = "CIRCUIT_BREAKER_OPEN";
    public static final String HEADER_VALUE_JWT_AUTH_FAILED = "JWT_AUTHENTICATION_FAILED";

    // ===================================================================
    // STATUS VALUES
    // ===================================================================
    public static final String STATUS_UP = "UP";
    public static final String STATUS_DOWN = "DOWN";
    public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    public static final String STATUS_NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    public static final String STATUS_HEALTHY = "HEALTHY";
    public static final String STATUS_PARTIAL = "PARTIAL";
    public static final String STATUS_DEGRADED = "DEGRADED";
    public static final String STATUS_CRITICAL = "CRITICAL";

    // ===================================================================
    // REDIS KEYS
    // ===================================================================
    public static final String REDIS_HEALTH_CHECK_KEY = "gateway:health:check";
    public static final String REDIS_HEALTH_CHECK_VALUE = "ping";
    public static final String REDIS_RATE_LIMIT_PREFIX = "gateway:rate-limit:";

    // ===================================================================
    // RATE LIMITING
    // ===================================================================
    public static final String RATE_LIMIT_KEY_ANONYMOUS = "anonymous";
    public static final String RATE_LIMIT_KEY_UNKNOWN = "unknown";

    // ===================================================================
    // JWT CONSTANTS
    // ===================================================================
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_QUERY_PARAM = "token";
    public static final int JWT_TOKEN_START_INDEX = 7; // After "Bearer "

    // ===================================================================
    // DEFAULT VALUES
    // ===================================================================
    public static final String DEFAULT_CORS_ORIGINS = "*";
    public static final String DEFAULT_JWT_SECRET = "dev-secret-key";
    public static final long DEFAULT_JWT_EXPIRATION = 86400000L; // 24 hours
    public static final int DEFAULT_RATE_LIMIT_REPLENISH = 10;
    public static final int DEFAULT_RATE_LIMIT_BURST = 20;
    public static final int DEFAULT_FALLBACK_RETRY_SECONDS = 60;

    // ===================================================================
    // ENVIRONMENT PROFILES
    // ===================================================================
    public static final String PROFILE_DEV = "dev";
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_DOCKER = "docker";

    // ===================================================================
    // LOGGING PATTERNS
    // ===================================================================
    public static final String LOG_PATTERN_CONSOLE = "%clr(%d{HH:mm:ss.SSS}){faint} %clr(%-5level) %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n";
    public static final String LOG_PATTERN_FILE = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n";

    // ===================================================================
    // ACTUATOR ENDPOINTS
    // ===================================================================
    public static final String ACTUATOR_HEALTH = "health";
    public static final String ACTUATOR_INFO = "info";
    public static final String ACTUATOR_METRICS = "metrics";
    public static final String ACTUATOR_PROMETHEUS = "prometheus";
    public static final String ACTUATOR_ALL = "*";

    // ===================================================================
    // TIMEOUT VALUES (in seconds)
    // ===================================================================
    public static final int HEALTH_CHECK_TIMEOUT = 5;
    public static final int REDIS_TIMEOUT = 3;
    public static final int DEFAULT_CONNECT_TIMEOUT = 10;
    public static final int DEFAULT_RESPONSE_TIMEOUT = 30;

    // ===================================================================
    // THREAD AND CONNECTION POOL SIZES
    // ===================================================================
    public static final int MAX_CONNECTIONS_DEV = 50;
    public static final int MAX_CONNECTIONS_PROD = 500;
    public static final int MIN_IDLE_CONNECTIONS = 5;
    public static final int MAX_IDLE_CONNECTIONS = 20;

    // ===================================================================
    // BEAN NAMES
    // ===================================================================
    public static final String BEAN_REDIS_RATE_LIMITER = "redisRateLimiter";
    public static final String BEAN_ADMIN_RATE_LIMITER = "adminRateLimiter";
    public static final String BEAN_USER_KEY_RESOLVER = "userKeyResolver";
    public static final String BEAN_IP_KEY_RESOLVER = "ipKeyResolver";
    public static final String BEAN_REDIS_TEMPLATE = "reactiveRedisTemplate";
    public static final String BEAN_WEB_CLIENT = "webClient";
    public static final String BEAN_JWT_DECODER = "jwtDecoder";
    public static final String BEAN_GATEWAY_FILTER_FACTORY = "gatewayFilterFactory";
}