package com.petshop.functions.customers;

import com.petshop.functions.shared.dto.*;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Customer Management (Docker deployment)
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "customers");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> getAllCustomers() {
        List<Cliente> clientes = clienteRepository.findAll();
        List<ClienteResponseDTO> response = clientes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Customer not found"));
        }
        return ResponseEntity.ok(toResponseDTO(clienteOpt.get()));
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody ClienteRequestDTO request) {
        try {
            if (request.getNome() == null || request.getEmail() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Name and email are required"));
            }

            if (clienteRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            Cliente cliente = new Cliente();
            cliente.setNome(request.getNome());
            cliente.setEmail(request.getEmail());
            cliente.setCpf(request.getCpf() != null ? request.getCpf() : "");
            cliente.setTelefone(request.getTelefone() != null ? request.getTelefone() : "");
            cliente.setEndereco(request.getEndereco());

            cliente = clienteRepository.save(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(cliente));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create customer: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody ClienteRequestDTO request) {
        try {
            Optional<Cliente> clienteOpt = clienteRepository.findById(id);
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Cliente cliente = clienteOpt.get();
            
            if (request.getNome() != null) cliente.setNome(request.getNome());
            if (request.getEmail() != null) cliente.setEmail(request.getEmail());
            if (request.getCpf() != null) cliente.setCpf(request.getCpf());
            if (request.getTelefone() != null) cliente.setTelefone(request.getTelefone());
            if (request.getEndereco() != null) cliente.setEndereco(request.getEndereco());

            cliente = clienteRepository.save(cliente);
            return ResponseEntity.ok(toResponseDTO(cliente));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update customer: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            if (!clienteRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }
            
            clienteRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Customer deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete customer: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClienteResponseDTO>> searchCustomers(@RequestParam String query) {
        List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCase(query);
        List<ClienteResponseDTO> response = clientes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setEmail(cliente.getEmail());
        dto.setCpf(cliente.getCpf());
        dto.setTelefone(cliente.getTelefone());
        dto.setEndereco(cliente.getEndereco());
        return dto;
    }
}
