package com.iam.gateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient Configuration for Gateway - Production Ready
 * Configures WebClient for communication with downstream services
 */
@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${webclient.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${webclient.response-timeout-seconds:30}")
    private int responseTimeoutSeconds;

    @Value("${webclient.read-timeout-seconds:30}")
    private int readTimeoutSeconds;

    @Value("${webclient.write-timeout-seconds:30}")
    private int writeTimeoutSeconds;

    @Value("${webclient.max-in-memory-size:1048576}")
    private int maxInMemorySize;

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configure HTTP client with comprehensive timeouts and connection pooling
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeoutSeconds, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandler())
                .filter(addGatewayHeaders());
    }

    /**
     * Log outgoing requests with detailed information
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Gateway WebClient Request: {} {} - Headers: {}",
                        clientRequest.method(),
                        clientRequest.url(),
                        clientRequest.headers());
            } else {
                log.info("Gateway WebClient Request: {} {}",
                        clientRequest.method(), clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * Log incoming responses with status and timing
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("Gateway WebClient Response: {} - Headers: {}",
                        clientResponse.statusCode(),
                        clientResponse.headers().asHttpHeaders());
            } else {
                log.info("Gateway WebClient Response: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Add gateway identification headers to all outbound requests
     */
    private ExchangeFilterFunction addGatewayHeaders() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.just(clientRequest.mutate()
                    .header("X-Gateway-Request", "true")
                    .header("X-Gateway-Version", "1.0.0")
                    .header("X-Request-ID", generateRequestId())
                    .build());
        });
    }

    /**
     * Comprehensive error handling for WebClient responses
     */
    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.warn("Gateway WebClient error response: {} {} from URL: {}",
                        clientResponse.statusCode().value(),
                        clientResponse.statusCode().getReasonPhrase(),
                        clientResponse.request().getURI());

                // Log additional error details for 5xx errors
                if (clientResponse.statusCode().is5xxServerError()) {
                    log.error("Downstream service error: {} - This may trigger circuit breaker",
                            clientResponse.statusCode());
                }
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Generate unique request ID for tracing
     */
    private String generateRequestId() {
        return "gw-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Specialized WebClient for health checks with shorter timeouts
     */
    @Bean
    public WebClient healthCheckWebClient() {
        HttpClient healthCheckHttpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(healthCheckHttpClient))
                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.debug("Health check request: {}", clientRequest.url());
                    return Mono.just(clientRequest);
                }))
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    log.debug("Health check response: {} for {}",
                            clientResponse.statusCode(), clientResponse.request().getURI());
                    return Mono.just(clientResponse);
                }))
                .build();
    }
}