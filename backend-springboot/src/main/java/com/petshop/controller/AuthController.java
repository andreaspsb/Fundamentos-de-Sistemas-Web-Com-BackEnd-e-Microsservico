package com.petshop.controller;

import com.petshop.dto.LoginRequestDTO;
import com.petshop.dto.LoginResponseDTO;
import com.petshop.dto.UsuarioRequestDTO;
import com.petshop.model.Usuario;
import com.petshop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica usuário e retorna token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário")
    public ResponseEntity<?> registrar(@Valid @RequestBody UsuarioRequestDTO usuarioRequest) {
        try {
            Usuario usuario = authService.registrar(usuarioRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuário registrado com sucesso");
            response.put("username", usuario.getUsername());
            response.put("email", usuario.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/validar")
    @Operation(summary = "Validar token", description = "Verifica se o token ainda é válido")
    public ResponseEntity<?> validarToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.isEmpty()) {
                Map<String, Boolean> response = new HashMap<>();
                response.put("valido", false);
                return ResponseEntity.ok(response);
            }
            
            String token = authHeader.replace("Bearer ", "");
            boolean valido = authService.validarToken(token);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("valido", valido);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("valido", false);
            return ResponseEntity.ok(response);
        }
    }
}
