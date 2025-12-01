package com.petshop.functions.shared.serviceclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Classe base para Service Clients com suporte a Circuit Breaker e Retry
 */
public abstract class BaseServiceClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    protected final ResilienceFactory resilienceFactory;
    protected final String serviceName;
    protected final CircuitBreaker circuitBreaker;
    protected final Retry retry;

    protected BaseServiceClient(String serviceName, String baseUrl, ResilienceFactory resilienceFactory) {
        this.serviceName = serviceName;
        this.resilienceFactory = resilienceFactory;
        this.circuitBreaker = resilienceFactory.getCircuitBreaker(serviceName);
        this.retry = resilienceFactory.getRetry(serviceName);
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Executa uma chamada HTTP com Circuit Breaker e Retry
     */
    protected <T> Optional<T> executeWithResilience(Supplier<T> supplier, T fallback) {
        try {
            Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
            decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);
            return Optional.ofNullable(decoratedSupplier.get());
        } catch (Exception e) {
            logger.error("Erro ao chamar {}: {}", serviceName, e.getMessage());
            return Optional.ofNullable(fallback);
        }
    }

    /**
     * GET request
     */
    protected HttpResponse<String> doGet(String url, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30));
        
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * POST request
     */
    protected HttpResponse<String> doPost(String url, Object body, String token) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(body);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30));
        
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * PUT request
     */
    protected HttpResponse<String> doPut(String url, Object body, String token) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(body);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30));
        
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * DELETE request
     */
    protected HttpResponse<String> doDelete(String url, String token) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30));
        
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Verifica se o serviço está disponível
     */
    public boolean isAvailable() {
        return circuitBreaker.getState() != CircuitBreaker.State.OPEN;
    }

    /**
     * Retorna o estado do circuit breaker
     */
    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }
}
