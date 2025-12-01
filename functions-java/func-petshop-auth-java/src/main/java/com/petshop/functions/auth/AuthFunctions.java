package com.petshop.functions.auth;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.*;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.model.Usuario;
import com.petshop.functions.shared.repository.ClienteRepository;
import com.petshop.functions.shared.repository.UsuarioRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import com.petshop.functions.shared.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions for Authentication
 */
@Component
public class AuthFunctions {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final JwtService jwtService;
    private final FunctionAuthorization functionAuthorization;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthFunctions(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            JwtService jwtService,
            FunctionAuthorization functionAuthorization) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.jwtService = jwtService;
        this.functionAuthorization = functionAuthorization;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * POST /api/auth/login
     * Authenticates user and returns JWT token
     */
    @FunctionName("login")
    public HttpResponseMessage login(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "auth/login"
            ) HttpRequestMessage<Optional<LoginRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing login request");

        try {
            Optional<LoginRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            LoginRequestDTO loginRequest = bodyOpt.get();
            
            if (loginRequest.getUsername() == null || loginRequest.getSenha() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Username and password are required"))
                        .build();
            }

            // Find user
            context.getLogger().info("Looking for user: " + loginRequest.getUsername());
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameAndAtivo(loginRequest.getUsername(), true);
        
        if (usuarioOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Invalid credentials"))
                    .build();
        }

        Usuario usuario = usuarioOpt.get();

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha())) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Invalid credentials"))
                    .build();
        }

        // Get clienteId if user is a Cliente
        Long clienteId = null;
        String clienteNome = null;
        if ("Cliente".equals(usuario.getRole())) {
            Optional<Cliente> clienteOpt = clienteRepository.findByEmail(usuario.getEmail());
            if (clienteOpt.isPresent()) {
                clienteId = clienteOpt.get().getId();
                clienteNome = clienteOpt.get().getNome();
            }
        }

        // Generate JWT
        String token = jwtService.generateToken(usuario.getUsername(), usuario.getRole(), clienteId);

        LoginResponseDTO response = new LoginResponseDTO(
                token,
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRole(),
                clienteId,
                clienteNome
        );

        context.getLogger().info("Login successful for user: " + usuario.getUsername());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Login error: " + e.getMessage());
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Internal server error", "message", e.getMessage() != null ? e.getMessage() : "Unknown error"))
                    .build();
        }
    }

    /**
     * POST /api/auth/register
     * Registers a new user (Cliente)
     */
    @FunctionName("register")
    public HttpResponseMessage register(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "auth/register"
            ) HttpRequestMessage<Optional<UsuarioRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing registration request");

        Optional<UsuarioRequestDTO> bodyOpt = request.getBody();
        if (bodyOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Request body is required"))
                    .build();
        }

        UsuarioRequestDTO registerRequest = bodyOpt.get();

        // Validate required fields
        if (registerRequest.getUsername() == null || registerRequest.getSenha() == null || registerRequest.getEmail() == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Username, password and email are required"))
                    .build();
        }

        // Check if username already exists
        if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
            return request.createResponseBuilder(HttpStatus.CONFLICT)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Username already exists"))
                    .build();
        }

        // Check if email already exists
        if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
            return request.createResponseBuilder(HttpStatus.CONFLICT)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Email already exists"))
                    .build();
        }

        // Create new user
        Usuario usuario = new Usuario();
        usuario.setUsername(registerRequest.getUsername());
        usuario.setSenha(passwordEncoder.encode(registerRequest.getSenha()));
        usuario.setEmail(registerRequest.getEmail());
        usuario.setRole("Cliente"); // Default role
        usuario.setAtivo(true);

        usuario = usuarioRepository.save(usuario);

        context.getLogger().info("User registered successfully: " + usuario.getUsername());

        // Generate JWT for immediate login
        String token = jwtService.generateToken(usuario.getUsername(), usuario.getRole(), null);

        LoginResponseDTO response = new LoginResponseDTO(
                token,
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRole(),
                null,
                null
        );

        return request.createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/auth/validate
     * Validates JWT token
     */
    @FunctionName("validateToken")
    public HttpResponseMessage validateToken(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "auth/validate"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing token validation request");

        Optional<String> tokenOpt = functionAuthorization.extractToken(request);
        
        if (tokenOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(new TokenValidationResponseDTO(false, null, null, null))
                    .build();
        }

        JwtService.TokenInfo tokenInfo = jwtService.validateAndExtract(tokenOpt.get());
        
        TokenValidationResponseDTO response = new TokenValidationResponseDTO(
                tokenInfo.isValid(),
                tokenInfo.username(),
                tokenInfo.role(),
                tokenInfo.clienteId()
        );

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/auth/me
     * Returns current user info from token
     */
    @FunctionName("getCurrentUser")
    public HttpResponseMessage getCurrentUser(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "auth/me"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing get current user request");

        return functionAuthorization.executeProtected(request, authResult -> {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(authResult.username());
            
            if (usuarioOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "User not found"))
                        .build();
            }

            Usuario usuario = usuarioOpt.get();
            
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "id", usuario.getId(),
                            "username", usuario.getUsername(),
                            "email", usuario.getEmail(),
                            "role", usuario.getRole(),
                            "clienteId", authResult.clienteId()
                    ))
                    .build();
        });
    }

    /**
     * POST /api/auth/change-password
     * Changes user password
     */
    @FunctionName("changePassword")
    public HttpResponseMessage changePassword(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "auth/change-password"
            ) HttpRequestMessage<Optional<Map<String, String>>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing change password request");

        return functionAuthorization.executeProtected(request, authResult -> {
            Optional<Map<String, String>> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            Map<String, String> body = bodyOpt.get();
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Current and new password are required"))
                        .build();
            }

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(authResult.username());
            if (usuarioOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "User not found"))
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, usuario.getSenha())) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Current password is incorrect"))
                        .build();
            }

            // Update password
            usuario.setSenha(passwordEncoder.encode(newPassword));
            usuarioRepository.save(usuario);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of("message", "Password changed successfully"))
                    .build();
        });
    }
}
