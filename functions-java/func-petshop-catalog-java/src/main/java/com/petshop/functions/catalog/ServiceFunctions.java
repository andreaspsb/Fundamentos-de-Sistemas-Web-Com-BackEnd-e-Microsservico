package com.petshop.functions.catalog;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.ServicoRequestDTO;
import com.petshop.functions.shared.dto.ServicoResponseDTO;
import com.petshop.functions.shared.dto.ServicoSimpleDTO;
import com.petshop.functions.shared.model.Servico;
import com.petshop.functions.shared.repository.ServicoRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import com.petshop.shared.util.ValidationUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Azure Functions for Service Management
 */
@Component
public class ServiceFunctions {

    private final ServicoRepository servicoRepository;
    private final FunctionAuthorization functionAuthorization;

    public ServiceFunctions(
            ServicoRepository servicoRepository,
            FunctionAuthorization functionAuthorization) {
        this.servicoRepository = servicoRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/servicos
     * List all active services (public)
     */
    @FunctionName("getAllServices")
    public HttpResponseMessage getAllServices(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all services");

        List<Servico> servicos = servicoRepository.findByAtivo(true);
        List<ServicoResponseDTO> response = servicos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/servicos/simple
     * List all active services in simplified format (public)
     */
    @FunctionName("getAllServicesSimple")
    public HttpResponseMessage getAllServicesSimple(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/simple"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all services (simple)");

        List<Servico> servicos = servicoRepository.findByAtivo(true);
        List<ServicoSimpleDTO> response = servicos.stream()
                .map(s -> new ServicoSimpleDTO(s.getId(), s.getNome(), s.getPreco()))
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/servicos/all
     * List all services including inactive (Admin only)
     */
    @FunctionName("getAllServicesAdmin")
    public HttpResponseMessage getAllServicesAdmin(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/all"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all services (admin)");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            List<Servico> servicos = servicoRepository.findAll();
            List<ServicoResponseDTO> response = servicos.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/servicos/ativos
     * List active services only (public) - alias for /servicos
     */
    @FunctionName("getActiveServices")
    public HttpResponseMessage getActiveServices(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/ativos"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting active services");

        List<Servico> servicos = servicoRepository.findByAtivo(true);
        List<ServicoResponseDTO> response = servicos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/servicos/{id}
     * Get service by ID (public)
     */
    @FunctionName("getServiceById")
    public HttpResponseMessage getServiceById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting service by ID: " + id);

        Optional<Servico> servicoOpt = servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"));
        
        if (servicoOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Serviço não encontrado"))
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(toResponseDTO(servicoOpt.get()))
                .build();
    }

    /**
     * POST /api/servicos
     * Create new service (Admin only)
     */
    @FunctionName("createService")
    public HttpResponseMessage createService(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos"
            ) HttpRequestMessage<Optional<ServicoRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new service");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<ServicoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            ServicoRequestDTO dto = bodyOpt.get();

            if (dto.getNome() == null || dto.getPreco() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Nome e preço são obrigatórios"))
                        .build();
            }

            if (servicoRepository.existsByNome(dto.getNome())) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Serviço já existe"))
                        .build();
            }

            Servico servico = new Servico();
            servico.setNome(dto.getNome());
            servico.setDescricao(dto.getDescricao());
            servico.setPreco(dto.getPreco());
            servico.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

            servico = servicoRepository.save(servico);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(servico))
                    .build();
        });
    }

    /**
     * PUT /api/servicos/{id}
     * Update service (Admin only)
     */
    @FunctionName("updateService")
    public HttpResponseMessage updateService(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/{id}"
            ) HttpRequestMessage<Optional<ServicoRequestDTO>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating service: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Servico> servicoOpt = servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"));
            if (servicoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Serviço não encontrado"))
                        .build();
            }

            Optional<ServicoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            ServicoRequestDTO dto = bodyOpt.get();
            Servico servico = servicoOpt.get();

            if (dto.getNome() != null) servico.setNome(dto.getNome());
            if (dto.getDescricao() != null) servico.setDescricao(dto.getDescricao());
            if (dto.getPreco() != null) servico.setPreco(dto.getPreco());
            if (dto.getAtivo() != null) servico.setAtivo(dto.getAtivo());

            Servico toSave = ValidationUtils.requireNonNullEntity(servico, "Serviço");
            servico = ValidationUtils.requireNonNullEntity(servicoRepository.save(toSave), "Serviço");

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(servico))
                    .build();
        });
    }

    /**
     * PATCH /api/servicos/{id}/ativar
     * Activate service (Admin only)
     */
    @FunctionName("activateService")
    public HttpResponseMessage activateService(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PATCH},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/{id}/ativar"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Activating service: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Servico> servicoOpt = servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"));
            if (servicoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Serviço não encontrado"))
                        .build();
            }

            Servico servico = servicoOpt.get();
            servico.setAtivo(true);
            servicoRepository.save(servico);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(servico))
                    .build();
        });
    }

    /**
     * PATCH /api/servicos/{id}/desativar
     * Deactivate service (Admin only)
     */
    @FunctionName("deactivateService")
    public HttpResponseMessage deactivateService(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PATCH},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/{id}/desativar"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deactivating service: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Servico> servicoOpt = servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"));
            if (servicoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Serviço não encontrado"))
                        .build();
            }

            Servico servico = servicoOpt.get();
            servico.setAtivo(false);
            servicoRepository.save(servico);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(servico))
                    .build();
        });
    }

    /**
     * DELETE /api/servicos/{id}
     * Delete service (Admin only)
     */
    @FunctionName("deleteService")
    public HttpResponseMessage deleteService(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "servicos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting service: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Long safeId = ValidationUtils.requireNonNullId(id, "Serviço");
            Optional<Servico> servicoOpt = servicoRepository.findById(safeId);
            if (servicoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Serviço não encontrado"))
                        .build();
            }

            servicoRepository.deleteById(safeId);

            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .build();
        });
    }

    private ServicoResponseDTO toResponseDTO(Servico servico) {
        return new ServicoResponseDTO(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco(),
                servico.getAtivo()
        );
    }
}
