package com.petshop.functions.customers;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.ClienteRequestDTO;
import com.petshop.functions.shared.dto.ClienteResponseDTO;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.repository.ClienteRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Azure Functions for Customer Management
 */
@Component
public class CustomerFunctions {

    private final ClienteRepository clienteRepository;
    private final FunctionAuthorization functionAuthorization;

    @Autowired
    public CustomerFunctions(
            ClienteRepository clienteRepository,
            FunctionAuthorization functionAuthorization) {
        this.clienteRepository = clienteRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/clientes
     * List all customers (Admin only)
     */
    @FunctionName("getAllCustomers")
    public HttpResponseMessage getAllCustomers(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all customers");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            List<Cliente> clientes = clienteRepository.findAll();
            List<ClienteResponseDTO> response = clientes.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/clientes/{id}
     * Get customer by ID
     */
    @FunctionName("getCustomerById")
    public HttpResponseMessage getCustomerById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting customer by ID: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            // Cliente can only see their own data
            if ("Cliente".equals(authResult.role()) && !id.equals(authResult.clienteId())) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode visualizar seus próprios dados");
            }

            Optional<Cliente> clienteOpt = clienteRepository.findById(id);
            
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(clienteOpt.get()))
                    .build();
        });
    }

    /**
     * GET /api/clientes/cpf/{cpf}
     * Get customer by CPF (Admin only)
     */
    @FunctionName("getCustomerByCpf")
    public HttpResponseMessage getCustomerByCpf(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes/cpf/{cpf}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("cpf") String cpf,
            final ExecutionContext context) {

        context.getLogger().info("Getting customer by CPF");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Cliente> clienteOpt = clienteRepository.findByCpf(cpf);
            
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(clienteOpt.get()))
                    .build();
        });
    }

    /**
     * GET /api/clientes/email/{email}
     * Get customer by email (Admin only)
     */
    @FunctionName("getCustomerByEmail")
    public HttpResponseMessage getCustomerByEmail(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes/email/{email}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("email") String email,
            final ExecutionContext context) {

        context.getLogger().info("Getting customer by email");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
            
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(clienteOpt.get()))
                    .build();
        });
    }

    /**
     * POST /api/clientes
     * Create new customer
     */
    @FunctionName("createCustomer")
    public HttpResponseMessage createCustomer(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes"
            ) HttpRequestMessage<Optional<ClienteRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new customer");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<ClienteRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            ClienteRequestDTO dto = bodyOpt.get();

            // Validate required fields
            if (dto.getNome() == null || dto.getCpf() == null || dto.getEmail() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Nome, CPF e email são obrigatórios"))
                        .build();
            }

            // Check if CPF already exists
            if (clienteRepository.existsByCpf(dto.getCpf())) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "CPF já cadastrado"))
                        .build();
            }

            // Check if email already exists
            if (clienteRepository.existsByEmail(dto.getEmail())) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Email já cadastrado"))
                        .build();
            }

            // Create cliente
            Cliente cliente = new Cliente();
            cliente.setNome(dto.getNome());
            cliente.setCpf(dto.getCpf());
            cliente.setEmail(dto.getEmail());
            cliente.setTelefone(dto.getTelefone());
            cliente.setEndereco(dto.getEndereco());
            cliente.setNumero(dto.getNumero());
            cliente.setComplemento(dto.getComplemento());
            cliente.setBairro(dto.getBairro());
            cliente.setCidade(dto.getCidade());
            cliente.setDataNascimento(dto.getDataNascimento());
            cliente.setSexo(dto.getSexo());

            cliente = clienteRepository.save(cliente);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(cliente))
                    .build();
        });
    }

    /**
     * PUT /api/clientes/{id}
     * Update customer
     */
    @FunctionName("updateCustomer")
    public HttpResponseMessage updateCustomer(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes/{id}"
            ) HttpRequestMessage<Optional<ClienteRequestDTO>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating customer: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            // Cliente can only update their own data
            if ("Cliente".equals(authResult.role()) && !id.equals(authResult.clienteId())) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode atualizar seus próprios dados");
            }

            Optional<Cliente> clienteOpt = clienteRepository.findById(id);
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            Optional<ClienteRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            ClienteRequestDTO dto = bodyOpt.get();
            Cliente cliente = clienteOpt.get();

            // Update fields
            if (dto.getNome() != null) cliente.setNome(dto.getNome());
            if (dto.getTelefone() != null) cliente.setTelefone(dto.getTelefone());
            if (dto.getEndereco() != null) cliente.setEndereco(dto.getEndereco());
            if (dto.getNumero() != null) cliente.setNumero(dto.getNumero());
            if (dto.getComplemento() != null) cliente.setComplemento(dto.getComplemento());
            if (dto.getBairro() != null) cliente.setBairro(dto.getBairro());
            if (dto.getCidade() != null) cliente.setCidade(dto.getCidade());
            if (dto.getDataNascimento() != null) cliente.setDataNascimento(dto.getDataNascimento());
            if (dto.getSexo() != null) cliente.setSexo(dto.getSexo());

            // CPF and email can only be changed by admin
            if ("Admin".equals(authResult.role())) {
                if (dto.getCpf() != null && !dto.getCpf().equals(cliente.getCpf())) {
                    if (clienteRepository.existsByCpf(dto.getCpf())) {
                        return request.createResponseBuilder(HttpStatus.CONFLICT)
                                .header("Content-Type", "application/json")
                                .body(Map.of("error", "CPF já cadastrado"))
                                .build();
                    }
                    cliente.setCpf(dto.getCpf());
                }
                if (dto.getEmail() != null && !dto.getEmail().equals(cliente.getEmail())) {
                    if (clienteRepository.existsByEmail(dto.getEmail())) {
                        return request.createResponseBuilder(HttpStatus.CONFLICT)
                                .header("Content-Type", "application/json")
                                .body(Map.of("error", "Email já cadastrado"))
                                .build();
                    }
                    cliente.setEmail(dto.getEmail());
                }
            }

            cliente = clienteRepository.save(cliente);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(cliente))
                    .build();
        });
    }

    /**
     * DELETE /api/clientes/{id}
     * Delete customer (Admin only)
     */
    @FunctionName("deleteCustomer")
    public HttpResponseMessage deleteCustomer(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "clientes/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting customer: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Cliente> clienteOpt = clienteRepository.findById(id);
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            clienteRepository.deleteById(id);

            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .build();
        });
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getDataNascimento(),
                cliente.getSexo(),
                cliente.getEndereco(),
                cliente.getNumero(),
                cliente.getComplemento(),
                cliente.getBairro(),
                cliente.getCidade()
        );
    }
}
