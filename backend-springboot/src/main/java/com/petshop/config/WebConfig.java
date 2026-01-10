package com.petshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração centralizada de CORS.
 * TODAS as origens permitidas estão definidas AQUI - não há outro lugar para configurar!
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Lista ÚNICA de origens permitidas para TODOS os ambientes.
     * Adicione novas origens SOMENTE AQUI.
     */
    private static final String[] ALLOWED_ORIGINS = {
        // === PRODUÇÃO ===
        "https://andreaspsb.github.io",
        "https://yellow-field-047215b0f.3.azurestaticapps.net",
        
        // === DESENVOLVIMENTO LOCAL ===
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:8080",
        "http://127.0.0.1:8080",
        "http://localhost:19006",
        "http://127.0.0.1:19006"
    };

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("X-Pagination", "X-Total-Count")
                .maxAge(3600);
    }
}
