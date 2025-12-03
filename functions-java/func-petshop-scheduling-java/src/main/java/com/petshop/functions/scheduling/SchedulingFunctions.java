package com.petshop.functions.scheduling;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.AgendamentoRequestDTO;
import com.petshop.functions.shared.dto.AgendamentoResponseDTO;
import com.petshop.functions.shared.dto.ServicoSimpleDTO;
import com.petshop.functions.shared.model.Agendamento;
import com.petshop.functions.shared.model.Agendamento.StatusAgendamento;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.model.Pet;
import com.petshop.functions.shared.model.Servico;
import com.petshop.functions.shared.repository.AgendamentoRepository;
import com.petshop.functions.shared.repository.ClienteRepository;
import com.petshop.functions.shared.repository.PetRepository;
import com.petshop.functions.shared.repository.ServicoRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Azure Functions for Scheduling Management
 */
@Component
public class SchedulingFunctions {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PetRepository petRepository;
    private final ServicoRepository servicoRepository;
    private final FunctionAuthorization functionAuthorization;

    @Autowired
    public SchedulingFunctions(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            PetRepository petRepository,
            ServicoRepository servicoRepository,
            FunctionAuthorization functionAuthorization) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.petRepository = petRepository;
        this.servicoRepository = servicoRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/agendamentos
     * List all appointments (Admin) or user's appointments (Cliente)
     */
    @FunctionName("getAllAppointments")
    public HttpResponseMessage getAllAppointments(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all appointments");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            List<Agendamento> agendamentos;
            
            if ("Admin".equals(authResult.role())) {
                agendamentos = agendamentoRepository.findAll();
            } else {
                if (authResult.clienteId() == null) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(List.of())
                            .build();
                }
                agendamentos = agendamentoRepository.findByClienteId(authResult.clienteId());
            }

            List<AgendamentoResponseDTO> response = agendamentos.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/agendamentos/{id}
     * Get appointment by ID
     */
    @FunctionName("getAppointmentById")
    public HttpResponseMessage getAppointmentById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting appointment by ID: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            
            if (agendamentoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Agendamento não encontrado"))
                        .build();
            }

            Agendamento agendamento = agendamentoOpt.get();

            // Cliente can only see their own appointments
            if ("Cliente".equals(authResult.role()) && 
                (agendamento.getCliente() == null || !agendamento.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode visualizar seus próprios agendamentos");
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(agendamento))
                    .build();
        });
    }

    /**
     * GET /api/agendamentos/cliente/{clienteId}
     * Get appointments by customer ID
     */
    @FunctionName("getAppointmentsByCustomerId")
    public HttpResponseMessage getAppointmentsByCustomerId(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/cliente/{clienteId}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("clienteId") Long clienteId,
            final ExecutionContext context) {

        context.getLogger().info("Getting appointments by customer ID: " + clienteId);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            // Cliente can only see their own appointments
            if ("Cliente".equals(authResult.role()) && !clienteId.equals(authResult.clienteId())) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode visualizar seus próprios agendamentos");
            }

            List<Agendamento> agendamentos = agendamentoRepository.findByClienteId(clienteId);
            List<AgendamentoResponseDTO> response = agendamentos.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/agendamentos/data/{data}
     * Get appointments by date (Admin only)
     */
    @FunctionName("getAppointmentsByDate")
    public HttpResponseMessage getAppointmentsByDate(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/data/{data}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("data") String dataStr,
            final ExecutionContext context) {

        context.getLogger().info("Getting appointments by date: " + dataStr);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            try {
                LocalDate data = LocalDate.parse(dataStr);
                List<Agendamento> agendamentos = agendamentoRepository.findByDataAgendamento(data);
                List<AgendamentoResponseDTO> response = agendamentos.stream()
                        .map(this::toResponseDTO)
                        .collect(Collectors.toList());

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(response)
                        .build();
            } catch (Exception e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Data inválida. Use o formato yyyy-MM-dd"))
                        .build();
            }
        });
    }

    /**
     * GET /api/agendamentos/status/{status}
     * Get appointments by status (Admin only)
     */
    @FunctionName("getAppointmentsByStatus")
    public HttpResponseMessage getAppointmentsByStatus(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/status/{status}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("status") String statusStr,
            final ExecutionContext context) {

        context.getLogger().info("Getting appointments by status: " + statusStr);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            try {
                StatusAgendamento status = StatusAgendamento.valueOf(statusStr.toUpperCase());
                List<Agendamento> agendamentos = agendamentoRepository.findByStatus(status);
                List<AgendamentoResponseDTO> response = agendamentos.stream()
                        .map(this::toResponseDTO)
                        .collect(Collectors.toList());

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(response)
                        .build();
            } catch (IllegalArgumentException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Status inválido. Use: PENDENTE, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO"))
                        .build();
            }
        });
    }

    /**
     * POST /api/agendamentos
     * Create new appointment
     */
    @FunctionName("createAppointment")
    public HttpResponseMessage createAppointment(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos"
            ) HttpRequestMessage<Optional<AgendamentoRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new appointment");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<AgendamentoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            AgendamentoRequestDTO dto = bodyOpt.get();

            // Validate required fields
            if (dto.getPetId() == null || dto.getServicoIds() == null || dto.getServicoIds().isEmpty() || 
                dto.getDataAgendamento() == null || dto.getHorario() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pet, serviço, data e hora são obrigatórios"))
                        .build();
            }

            // Determine clienteId
            Long clienteId = dto.getClienteId();
            if ("Cliente".equals(authResult.role())) {
                clienteId = authResult.clienteId();
            }

            if (clienteId == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não identificado"))
                        .build();
            }

            // Validate entities exist
            Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            Optional<Pet> petOpt = petRepository.findById(dto.getPetId());
            if (petOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pet não encontrado"))
                        .build();
            }

            // Validate pet belongs to customer
            Pet pet = petOpt.get();
            if ("Cliente".equals(authResult.role()) && 
                (pet.getCliente() == null || !pet.getCliente().getId().equals(clienteId))) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Este pet não pertence a você"))
                        .build();
            }

            // Load services
            List<Servico> servicos = new ArrayList<>();
            double valorTotal = 0.0;
            for (Long servicoId : dto.getServicoIds()) {
                Optional<Servico> servicoOpt = servicoRepository.findById(servicoId);
                if (servicoOpt.isEmpty()) {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .header("Content-Type", "application/json")
                            .body(Map.of("error", "Serviço não encontrado: " + servicoId))
                            .build();
                }
                servicos.add(servicoOpt.get());
                valorTotal += servicoOpt.get().getPreco();
            }

            // Check for conflicting appointments
            List<Agendamento> conflitos = agendamentoRepository.findByDataAndHorario(
                    dto.getDataAgendamento(), dto.getHorario());
            if (!conflitos.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Já existe um agendamento para esta data e horário"))
                        .build();
            }

            Agendamento agendamento = new Agendamento();
            agendamento.setCliente(clienteOpt.get());
            agendamento.setPet(pet);
            agendamento.setServicos(servicos);
            agendamento.setDataAgendamento(dto.getDataAgendamento());
            agendamento.setHorario(dto.getHorario());
            agendamento.setMetodoAtendimento(dto.getMetodoAtendimento() != null ? dto.getMetodoAtendimento() : "local");
            agendamento.setPortePet(dto.getPortePet());
            agendamento.setObservacoes(dto.getObservacoes());
            agendamento.setValorTotal(valorTotal);
            agendamento.setStatus(StatusAgendamento.PENDENTE);

            agendamento = agendamentoRepository.save(agendamento);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(agendamento))
                    .build();
        });
    }

    /**
     * PUT /api/agendamentos/{id}
     * Update appointment
     */
    @FunctionName("updateAppointment")
    public HttpResponseMessage updateAppointment(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/{id}"
            ) HttpRequestMessage<Optional<AgendamentoRequestDTO>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating appointment: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (agendamentoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Agendamento não encontrado"))
                        .build();
            }

            Agendamento agendamento = agendamentoOpt.get();

            // Cliente can only update their own appointments
            if ("Cliente".equals(authResult.role()) && 
                (agendamento.getCliente() == null || !agendamento.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode atualizar seus próprios agendamentos");
            }

            Optional<AgendamentoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            AgendamentoRequestDTO dto = bodyOpt.get();

            // Update fields
            if (dto.getDataAgendamento() != null) agendamento.setDataAgendamento(dto.getDataAgendamento());
            if (dto.getHorario() != null) agendamento.setHorario(dto.getHorario());
            if (dto.getMetodoAtendimento() != null) agendamento.setMetodoAtendimento(dto.getMetodoAtendimento());
            if (dto.getPortePet() != null) agendamento.setPortePet(dto.getPortePet());
            if (dto.getObservacoes() != null) agendamento.setObservacoes(dto.getObservacoes());

            agendamento = agendamentoRepository.save(agendamento);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(agendamento))
                    .build();
        });
    }

    /**
     * PUT /api/agendamentos/{id}/status
     * Update appointment status (Admin only)
     */
    @FunctionName("updateAppointmentStatus")
    public HttpResponseMessage updateAppointmentStatus(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/{id}/status"
            ) HttpRequestMessage<Optional<Map<String, String>>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating appointment status: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (agendamentoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Agendamento não encontrado"))
                        .build();
            }

            Optional<Map<String, String>> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty() || !bodyOpt.get().containsKey("status")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "status é obrigatório"))
                        .build();
            }

            try {
                Agendamento agendamento = agendamentoOpt.get();
                StatusAgendamento status = StatusAgendamento.valueOf(bodyOpt.get().get("status").toUpperCase());
                agendamento.setStatus(status);
                agendamentoRepository.save(agendamento);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(toResponseDTO(agendamento))
                        .build();
            } catch (IllegalArgumentException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Status inválido. Use: PENDENTE, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO"))
                        .build();
            }
        });
    }

    /**
     * DELETE /api/agendamentos/{id}
     * Cancel/Delete appointment
     */
    @FunctionName("deleteAppointment")
    public HttpResponseMessage deleteAppointment(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "agendamentos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting appointment: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
            if (agendamentoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Agendamento não encontrado"))
                        .build();
            }

            Agendamento agendamento = agendamentoOpt.get();

            // Cliente can only cancel their own appointments
            if ("Cliente".equals(authResult.role()) && 
                (agendamento.getCliente() == null || !agendamento.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode cancelar seus próprios agendamentos");
            }

            // For Cliente, change status to Cancelado instead of deleting
            if ("Cliente".equals(authResult.role())) {
                agendamento.setStatus(StatusAgendamento.CANCELADO);
                agendamentoRepository.save(agendamento);
            } else {
                agendamentoRepository.deleteById(id);
            }

            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .build();
        });
    }

    private AgendamentoResponseDTO toResponseDTO(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setDataAgendamento(agendamento.getDataAgendamento());
        dto.setHorario(agendamento.getHorario());
        dto.setMetodoAtendimento(agendamento.getMetodoAtendimento());
        dto.setPortePet(agendamento.getPortePet());
        dto.setObservacoes(agendamento.getObservacoes());
        dto.setValorTotal(agendamento.getValorTotal());
        dto.setStatus(agendamento.getStatus() != null ? agendamento.getStatus().name() : null);
        
        if (agendamento.getCliente() != null) {
            dto.setClienteId(agendamento.getCliente().getId());
            dto.setClienteNome(agendamento.getCliente().getNome());
            dto.setClienteTelefone(agendamento.getCliente().getTelefone());
        }
        
        if (agendamento.getPet() != null) {
            dto.setPetId(agendamento.getPet().getId());
            dto.setPetNome(agendamento.getPet().getNome());
        }
        
        if (agendamento.getServicos() != null && !agendamento.getServicos().isEmpty()) {
            List<ServicoSimpleDTO> servicos = agendamento.getServicos().stream()
                    .map(s -> new ServicoSimpleDTO(s.getId(), s.getNome(), s.getPreco()))
                    .collect(Collectors.toList());
            dto.setServicos(servicos);
        }
        
        return dto;
    }
}
