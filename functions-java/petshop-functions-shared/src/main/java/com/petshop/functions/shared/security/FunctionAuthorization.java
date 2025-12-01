package com.petshop.functions.shared.security;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Helper class para autorização em Azure Functions
 * Valida JWT e verifica roles
 */
@Component
public class FunctionAuthorization {

    private final JwtService jwtService;

    public FunctionAuthorization(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Extrai o token do header Authorization
     */
    public Optional<String> extractToken(HttpRequestMessage<?> request) {
        String authHeader = request.getHeaders().get("authorization");
        if (authHeader == null) {
            authHeader = request.getHeaders().get("Authorization");
        }
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }

    /**
     * Valida o token e retorna informações do usuário
     */
    public AuthorizationResult authorize(HttpRequestMessage<?> request) {
        Optional<String> tokenOpt = extractToken(request);
        
        if (tokenOpt.isEmpty()) {
            return AuthorizationResult.unauthorized("Token não fornecido");
        }
        
        try {
            JwtService.TokenInfo tokenInfo = jwtService.validateAndExtract(tokenOpt.get());
            
            if (!tokenInfo.isValid()) {
                return AuthorizationResult.unauthorized("Token inválido ou expirado");
            }
            
            return AuthorizationResult.authorized(
                tokenInfo.username(),
                tokenInfo.role(),
                tokenInfo.clienteId()
            );
        } catch (Exception e) {
            return AuthorizationResult.unauthorized("Erro ao validar token: " + e.getMessage());
        }
    }

    /**
     * Valida o token e verifica se o usuário tem uma das roles permitidas
     */
    public AuthorizationResult authorizeWithRoles(HttpRequestMessage<?> request, Set<String> allowedRoles) {
        AuthorizationResult result = authorize(request);
        
        if (!result.isAuthorized()) {
            return result;
        }
        
        if (!allowedRoles.contains(result.role())) {
            return AuthorizationResult.forbidden("Acesso negado. Role necessária: " + allowedRoles);
        }
        
        return result;
    }

    /**
     * Valida que o usuário é Admin
     */
    public AuthorizationResult authorizeAdmin(HttpRequestMessage<?> request) {
        return authorizeWithRoles(request, Set.of("Admin"));
    }

    /**
     * Valida que o usuário é Cliente ou Admin
     */
    public AuthorizationResult authorizeClienteOrAdmin(HttpRequestMessage<?> request) {
        return authorizeWithRoles(request, Set.of("Cliente", "Admin"));
    }

    /**
     * Executa uma função protegida por autenticação
     */
    public <T> HttpResponseMessage executeProtected(
            HttpRequestMessage<T> request,
            Function<AuthorizationResult, HttpResponseMessage> handler) {
        
        AuthorizationResult authResult = authorize(request);
        
        if (!authResult.isAuthorized()) {
            return createUnauthorizedResponse(request, authResult.errorMessage());
        }
        
        return handler.apply(authResult);
    }

    /**
     * Executa uma função protegida por roles específicas
     */
    public <T> HttpResponseMessage executeProtectedWithRoles(
            HttpRequestMessage<T> request,
            Set<String> allowedRoles,
            Function<AuthorizationResult, HttpResponseMessage> handler) {
        
        AuthorizationResult authResult = authorizeWithRoles(request, allowedRoles);
        
        if (!authResult.isAuthorized()) {
            if (authResult.isForbidden()) {
                return createForbiddenResponse(request, authResult.errorMessage());
            }
            return createUnauthorizedResponse(request, authResult.errorMessage());
        }
        
        return handler.apply(authResult);
    }

    /**
     * Executa uma função protegida para Admin
     */
    public <T> HttpResponseMessage executeProtectedAdmin(
            HttpRequestMessage<T> request,
            Function<AuthorizationResult, HttpResponseMessage> handler) {
        return executeProtectedWithRoles(request, Set.of("Admin"), handler);
    }

    /**
     * Cria resposta 401 Unauthorized
     */
    public <T> HttpResponseMessage createUnauthorizedResponse(HttpRequestMessage<T> request, String message) {
        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                .header("Content-Type", "application/json")
                .body(Map.of("error", message != null ? message : "Não autorizado"))
                .build();
    }

    /**
     * Cria resposta 403 Forbidden
     */
    public <T> HttpResponseMessage createForbiddenResponse(HttpRequestMessage<T> request, String message) {
        return request.createResponseBuilder(HttpStatus.FORBIDDEN)
                .header("Content-Type", "application/json")
                .body(Map.of("error", message != null ? message : "Acesso negado"))
                .build();
    }

    /**
     * Record que encapsula o resultado da autorização
     */
    public record AuthorizationResult(
            boolean isAuthorized,
            boolean isForbidden,
            String username,
            String role,
            Long clienteId,
            String errorMessage
    ) {
        public static AuthorizationResult authorized(String username, String role, Long clienteId) {
            return new AuthorizationResult(true, false, username, role, clienteId, null);
        }

        public static AuthorizationResult unauthorized(String message) {
            return new AuthorizationResult(false, false, null, null, null, message);
        }

        public static AuthorizationResult forbidden(String message) {
            return new AuthorizationResult(false, true, null, null, null, message);
        }
    }
}
