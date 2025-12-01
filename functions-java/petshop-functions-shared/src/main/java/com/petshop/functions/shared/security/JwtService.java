package com.petshop.functions.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${jwt.secret:petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas em ms
    private Long expiration;

    @Value("${jwt.issuer:PetshopApi}")
    private String issuer;

    @Value("${jwt.audience:PetshopFrontend}")
    private String audience;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera token JWT com username, role e clienteId
     */
    public String generateToken(String username, String role, Long clienteId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        if (clienteId != null) {
            claims.put("clienteId", clienteId);
        }
        return createToken(claims, username);
    }

    /**
     * Gera token JWT (sem clienteId)
     */
    public String generateToken(String username, String role) {
        return generateToken(username, role, null);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long extractClienteId(String token) {
        return extractClaim(token, claims -> {
            Object clienteId = claims.get("clienteId");
            if (clienteId == null) return null;
            if (clienteId instanceof Long) return (Long) clienteId;
            if (clienteId instanceof Integer) return ((Integer) clienteId).longValue();
            if (clienteId instanceof String) return Long.parseLong((String) clienteId);
            return null;
        });
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida e extrai informações do token
     * @return TokenInfo com isValid, username, role, clienteId
     */
    public TokenInfo validateAndExtract(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (isTokenExpired(token)) {
                return new TokenInfo(false, null, null, null);
            }
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Long clienteId = extractClienteId(token);
            return new TokenInfo(true, username, role, clienteId);
        } catch (Exception e) {
            return new TokenInfo(false, null, null, null);
        }
    }

    /**
     * Record para retornar informações do token
     */
    public record TokenInfo(boolean isValid, String username, String role, Long clienteId) {}
}
