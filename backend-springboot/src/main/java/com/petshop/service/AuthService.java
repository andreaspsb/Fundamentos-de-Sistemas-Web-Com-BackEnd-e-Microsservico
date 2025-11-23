package com.petshop.service;

import com.petshop.dto.LoginRequestDTO;
import com.petshop.dto.LoginResponseDTO;
import com.petshop.dto.UsuarioRequestDTO;
import com.petshop.model.Cliente;
import com.petshop.model.Usuario;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.UsuarioRepository;
import com.petshop.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Buscar usuário
        Usuario usuario = usuarioRepository.findByUsernameAndAtivo(loginRequest.getUsername(), true)
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        // Validar senha com BCrypt
        if (!passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        // Atualizar último acesso
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Gerar token JWT
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRole());

        // Retornar resposta
        Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
        
        return new LoginResponseDTO(
            token,
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getRole(),
            clienteId
        );
    }

    @Transactional
    public Usuario registrar(UsuarioRequestDTO usuarioRequest) {
        // Validar se username já existe
        if (usuarioRepository.existsByUsername(usuarioRequest.getUsername())) {
            throw new RuntimeException("Username já cadastrado");
        }

        // Validar se email já existe
        if (usuarioRepository.existsByEmail(usuarioRequest.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Criar novo usuário
        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioRequest.getUsername());
        // Hash da senha com BCrypt
        usuario.setSenha(passwordEncoder.encode(usuarioRequest.getSenha()));
        usuario.setEmail(usuarioRequest.getEmail());
        usuario.setRole(usuarioRequest.getRole());
        usuario.setAtivo(true);

        // Vincular cliente se fornecido
        if (usuarioRequest.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(usuarioRequest.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            usuario.setCliente(cliente);
        }

        return usuarioRepository.save(usuario);
    }

    public boolean validarToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public Optional<Usuario> getUsuarioFromToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            return usuarioRepository.findByUsername(username);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
