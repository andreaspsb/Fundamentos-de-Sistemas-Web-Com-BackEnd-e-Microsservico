package com.petshop.controller;

import com.petshop.dto.LoginRequestDTO;
import com.petshop.dto.LoginResponseDTO;
import com.petshop.security.JwtAuthenticationFilter;
import com.petshop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testLoginComSucesso() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setSenha("senha123");

        LoginResponseDTO loginResponse = new LoginResponseDTO(
            "fake-jwt-token",
            "testuser",
            "test@petshop.com",
            "CLIENTE",
            null
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@petshop.com"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    void testLoginComCredenciaisInvalidas() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("usernaoinexiste");
        loginRequest.setSenha("senhaerrada");

        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new RuntimeException("Usuário ou senha inválidos"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuário ou senha inválidos"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    void testValidarTokenValido() throws Exception {
        // Arrange
        String token = "Bearer valid-token";
        when(authService.validarToken("valid-token")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validar")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        verify(authService, times(1)).validarToken("valid-token");
    }

    @Test
    void testValidarTokenInvalido() throws Exception {
        // Arrange
        String token = "Bearer invalid-token";
        when(authService.validarToken("invalid-token")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validar")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));

        verify(authService, times(1)).validarToken("invalid-token");
    }

    @Test
    void testValidarTokenSemHeader() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));

        verify(authService, never()).validarToken(anyString());
    }
}
