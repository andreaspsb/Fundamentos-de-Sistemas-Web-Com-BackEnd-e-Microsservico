package com.petshop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET = "petshop-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm";
    private final Long EXPIRATION = 86400000L; // 24 horas

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void testGenerateToken() {
        // Arrange
        String username = "testuser";
        String role = "CLIENTE";

        // Act
        String token = jwtUtil.generateToken(username, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tem 3 partes separadas por .
    }

    @Test
    void testExtractUsername() {
        // Arrange
        String username = "testuser";
        String role = "CLIENTE";
        String token = jwtUtil.generateToken(username, role);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractRole() {
        // Arrange
        String username = "admin";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        // Act
        String extractedRole = jwtUtil.extractRole(token);

        // Assert
        assertEquals(role, extractedRole);
    }

    @Test
    void testValidateTokenValido() {
        // Arrange
        String username = "testuser";
        String role = "CLIENTE";
        String token = jwtUtil.generateToken(username, role);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, username);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenComUsernameIncorreto() {
        // Arrange
        String username = "testuser";
        String role = "CLIENTE";
        String token = jwtUtil.generateToken(username, role);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, "outrouser");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenInvalido() {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";

        // Act
        Boolean isValid = jwtUtil.validateToken(tokenInvalido);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testTokenNaoExpirado() {
        // Arrange
        String username = "testuser";
        String role = "CLIENTE";
        String token = jwtUtil.generateToken(username, role);

        // Act
        Boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testExtractExpiration() {
        // Arrange
        String username = "testuser";
        String role = "CLIENTE";
        String token = jwtUtil.generateToken(username, role);

        // Act
        var expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }

    @Test
    void testMultiplosTokensParaDiferentesUsuarios() {
        // Arrange & Act
        String token1 = jwtUtil.generateToken("user1", "CLIENTE");
        String token2 = jwtUtil.generateToken("user2", "ADMIN");

        String username1 = jwtUtil.extractUsername(token1);
        String username2 = jwtUtil.extractUsername(token2);
        String role1 = jwtUtil.extractRole(token1);
        String role2 = jwtUtil.extractRole(token2);

        // Assert
        assertEquals("user1", username1);
        assertEquals("user2", username2);
        assertEquals("CLIENTE", role1);
        assertEquals("ADMIN", role2);
        assertNotEquals(token1, token2);
    }
}
