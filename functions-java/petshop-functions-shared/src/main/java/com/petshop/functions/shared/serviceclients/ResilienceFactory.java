package com.petshop.functions.shared.serviceclients;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * Factory para criar Circuit Breakers e Retries para os Service Clients
 */
@Component
public class ResilienceFactory {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public ResilienceFactory() {
        // Configuração do Circuit Breaker
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% de falhas para abrir
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 30s aberto
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .recordExceptions(IOException.class, RuntimeException.class)
                .build();

        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);

        // Configuração do Retry
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(IOException.class)
                .build();

        this.retryRegistry = RetryRegistry.of(retryConfig);
    }

    public CircuitBreaker getCircuitBreaker(String name) {
        return circuitBreakerRegistry.circuitBreaker(name);
    }

    public Retry getRetry(String name) {
        return retryRegistry.retry(name);
    }

    public CircuitBreakerRegistry getCircuitBreakerRegistry() {
        return circuitBreakerRegistry;
    }

    public RetryRegistry getRetryRegistry() {
        return retryRegistry;
    }
}
