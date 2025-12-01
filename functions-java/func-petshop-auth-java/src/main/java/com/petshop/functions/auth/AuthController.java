package com.petshop.functions.auth;

import com.petshop.functions.shared.dto.*;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.model.Usuario;
import com.petshop.functions.shared.repository.ClienteRepository;
import com.petshop.functions.shared.repository.UsuarioRepository;
import com.petshop.functions.shared.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Authentication (Docker deployment)
 * Mirrors the Azure Functions endpoints for local/Docker deployment
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "auth");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            if (request.getUsername() == null || request.getSenha() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
            }

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.getUsername());
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }

            Usuario usuario = usuarioOpt.get();
            if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }

            String token = jwtService.generateToken(usuario.getUsername(), usuario.getRole(), usuario.getClienteId());

            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setUsername(usuario.getUsername());
            response.setEmail(usuario.getEmail());
            response.setRole(usuario.getRole());

            if (usuario.getClienteId() != null) {
                response.setClienteId(usuario.getClienteId());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioRequestDTO request) {
        try {
            if (request.getUsername() == null || request.getSenha() == null || request.getEmail() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username, password and email are required"));
            }

            if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already exists"));
            }

            if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already exists"));
            }

            // Create Cliente first
            Cliente cliente = new Cliente();
            cliente.setNome(request.getUsername());
            cliente.setEmail(request.getEmail());
            cliente.setCpf("");
            cliente.setTelefone("");
            cliente = clienteRepository.save(cliente);

            // Create Usuario
            Usuario usuario = new Usuario();
            usuario.setUsername(request.getUsername());
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
            usuario.setEmail(request.getEmail());
            usuario.setRole("USER");
            usuario.setCliente(cliente);
            usuario = usuarioRepository.save(usuario);

            String token = jwtService.generateToken(usuario.getUsername(), usuario.getRole(), cliente.getId());

            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            response.setUsername(usuario.getUsername());
            response.setEmail(usuario.getEmail());
            response.setRole(usuario.getRole());
            response.setClienteId(cliente.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtService.validateToken(token);

            TokenValidationResponseDTO response = new TokenValidationResponseDTO();
            response.setValid(isValid);

            if (isValid) {
                String username = jwtService.extractUsername(token);
                response.setUsername(username);
                
                Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    response.setRole(usuario.getRole());
                    response.setClienteId(usuario.getClienteId());
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authorization required"));
            }

            String token = authHeader.substring(7);
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
            }

            String username = jwtService.extractUsername(token);
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            Usuario usuario = usuarioOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("username", usuario.getUsername());
            response.put("email", usuario.getEmail());
            response.put("role", usuario.getRole());
            response.put("clienteId", usuario.getClienteId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get user: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authorization required"));
            }

            String token = authHeader.substring(7);
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current and new password are required"));
            }

            String username = jwtService.extractUsername(token);
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            Usuario usuario = usuarioOpt.get();
            
            if (!passwordEncoder.matches(currentPassword, usuario.getSenha())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Current password is incorrect"));
            }

            usuario.setSenha(passwordEncoder.encode(newPassword));
            usuarioRepository.save(usuario);

            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to change password: " + e.getMessage()));
        }
    }
}
