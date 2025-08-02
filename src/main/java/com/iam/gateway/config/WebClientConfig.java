package com.iam.gateway.config;

import com.iam.gateway.constants.GatewayConstants;
import com.iam.gateway.constants.GatewayMessages;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {

    private final ApiGatewayProperties properties;

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configure HTTP client with timeouts from properties
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getWebClient().getConnectTimeoutMs())
                .responseTimeout(Duration.ofSeconds(properties.getWebClient().getResponseTimeoutSeconds()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(properties.getWebClient().getReadTimeoutSeconds(), TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(properties.getWebClient().getWriteTimeoutSeconds(), TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(properties.getWebClient().getMaxInMemorySize()))
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandler())
                .filter(addGatewayHeaders());
    }

    /**
     * Log outgoing requests with detailed information - Using Constants
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
     * Log incoming responses with status and timing - Using Constants
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
     * Add gateway identification headers to all outbound requests - Using Constants
     */
    private ExchangeFilterFunction addGatewayHeaders() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ClientRequest newRequest = ClientRequest.from(clientRequest)
                    .header(GatewayConstants.HEADER_GATEWAY_REQUEST, GatewayConstants.HEADER_VALUE_TRUE)
                    .header(GatewayConstants.HEADER_GATEWAY_VERSION, GatewayConstants.APPLICATION_VERSION)
                    .header(GatewayConstants.HEADER_REQUEST_ID, generateRequestId())
                    .build();
            return Mono.just(newRequest);
        });
    }
    /**
     * Comprehensive error handling for WebClient responses - Using Messages
     */
    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.warn(GatewayMessages.LOG_WEBCLIENT_ERROR,
                        clientResponse.statusCode().value(),
                        "", // Reason phrase not available in Spring 6+
                        clientResponse.request().getURI());

                if (clientResponse.statusCode().is5xxServerError()) {
                    log.error(GatewayMessages.LOG_DOWNSTREAM_SERVICE_ERROR, clientResponse.statusCode());
                }
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Generate unique request ID for tracing - Using Constants
     */
    private String generateRequestId() {
        return GatewayMessages.REQUEST_ID_PREFIX +
                System.currentTimeMillis() +
                GatewayMessages.REQUEST_ID_SEPARATOR +
                Thread.currentThread().threadId();
    }

    /**
     * Specialized WebClient for health checks with shorter timeouts - Using Constants
     */
    @Bean(GatewayMessages.BEAN_HEALTH_CHECK_WEBCLIENT)
    public WebClient healthCheckWebClient() {
        HttpClient healthCheckHttpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(GatewayConstants.HEALTH_CHECK_TIMEOUT))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(GatewayConstants.HEALTH_CHECK_TIMEOUT, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(GatewayConstants.HEALTH_CHECK_TIMEOUT, TimeUnit.SECONDS))
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