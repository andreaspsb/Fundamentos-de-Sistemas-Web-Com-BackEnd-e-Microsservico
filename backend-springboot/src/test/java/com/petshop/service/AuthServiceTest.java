package com.petshop.service;

import com.petshop.dto.LoginRequestDTO;
import com.petshop.dto.LoginResponseDTO;
import com.petshop.model.Usuario;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.UsuarioRepository;
import com.petshop.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        usuario.setEmail("test@petshop.com");
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuario.setRole("CLIENTE");
        usuario.setAtivo(true);
    }

    @Test
    void testLoginComSucesso() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setSenha("senha123");

        when(usuarioRepository.findByUsernameAndAtivo(anyString(), anyBoolean()))
            .thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(anyString(), anyString()))
            .thenReturn("fake-jwt-token");
        when(usuarioRepository.save(any(Usuario.class)))
            .thenReturn(usuario);

        // Act
        LoginResponseDTO response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@petshop.com", response.getEmail());
        assertEquals("CLIENTE", response.getRole());
        
        verify(usuarioRepository, times(1)).findByUsernameAndAtivo("testuser", true);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(jwtUtil, times(1)).generateToken("testuser", "CLIENTE");
    }

    @Test
    void testLoginComUsuarioInexistente() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("usernaoinexiste");
        loginRequest.setSenha("senha123");

        when(usuarioRepository.findByUsernameAndAtivo(anyString(), anyBoolean()))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Usuário ou senha inválidos", exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsernameAndAtivo("usernaoinexiste", true);
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLoginComSenhaInvalida() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setSenha("senhaerrada");

        when(usuarioRepository.findByUsernameAndAtivo(anyString(), anyBoolean()))
            .thenReturn(Optional.of(usuario));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Usuário ou senha inválidos", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testValidarTokenValido() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // Act
        boolean resultado = authService.validarToken(token);

        // Assert
        assertTrue(resultado);
        verify(jwtUtil, times(1)).validateToken(token);
    }

    @Test
    void testValidarTokenInvalido() {
        // Arrange
        String token = "invalid-token";
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        boolean resultado = authService.validarToken(token);

        // Assert
        assertFalse(resultado);
        verify(jwtUtil, times(1)).validateToken(token);
    }

    @Test
    void testGetUsuarioFromToken() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));

        // Act
        Optional<Usuario> resultado = authService.getUsuarioFromToken(token);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("testuser", resultado.get().getUsername());
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(usuarioRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUsuarioFromTokenInvalido() {
        // Arrange
        String token = "invalid-token";
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Token inválido"));

        // Act
        Optional<Usuario> resultado = authService.getUsuarioFromToken(token);

        // Assert
        assertFalse(resultado.isPresent());
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(usuarioRepository, never()).findByUsername(anyString());
    }
}
