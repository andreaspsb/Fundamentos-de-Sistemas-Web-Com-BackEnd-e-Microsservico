package com.petshop.functions.scheduling;

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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SchedulingController {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PetRepository petRepository;
    private final ServicoRepository servicoRepository;

    public SchedulingController(AgendamentoRepository agendamentoRepository,
                                ClienteRepository clienteRepository,
                                PetRepository petRepository,
                                ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.petRepository = petRepository;
        this.servicoRepository = servicoRepository;
    }

    // === HEALTH CHECK ===
    
    @GetMapping("/scheduling/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "scheduling");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // === AGENDAMENTOS ===
    
    @GetMapping("/agendamentos")
    public ResponseEntity<List<AgendamentoResponseDTO>> getAllAgendamentos(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String data) {
        
        List<Agendamento> agendamentos;
        
        if (clienteId != null) {
            agendamentos = agendamentoRepository.findByClienteId(clienteId);
        } else if (status != null) {
            try {
                StatusAgendamento statusEnum = StatusAgendamento.valueOf(status.toUpperCase());
                agendamentos = agendamentoRepository.findByStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                agendamentos = agendamentoRepository.findAll();
            }
        } else if (data != null) {
            LocalDate dataAgendamento = LocalDate.parse(data);
            agendamentos = agendamentoRepository.findByDataAgendamento(dataAgendamento);
        } else {
            agendamentos = agendamentoRepository.findAll();
        }
        
        List<AgendamentoResponseDTO> response = agendamentos.stream()
            .map(this::toAgendamentoResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agendamentos/{id}")
    public ResponseEntity<?> getAgendamentoById(@PathVariable Long id) {
        Optional<Agendamento> agendamento = agendamentoRepository.findById(id);
        
        if (agendamento.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Agendamento não encontrado"));
        }
        
        return ResponseEntity.ok(toAgendamentoResponse(agendamento.get()));
    }

    @PostMapping("/agendamentos")
    public ResponseEntity<?> createAgendamento(@Valid @RequestBody AgendamentoRequestDTO request) {
        // Validar cliente
        Optional<Cliente> cliente = clienteRepository.findById(request.getClienteId());
        if (cliente.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Cliente não encontrado"));
        }
        
        // Validar pet
        Optional<Pet> pet = petRepository.findById(request.getPetId());
        if (pet.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Pet não encontrado"));
        }
        
        // Validar serviços
        List<Long> servicoIds = request.getServicoIds();
        if (servicoIds == null || servicoIds.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Selecione pelo menos um serviço"));
        }
        
        List<Servico> servicos = servicoRepository.findAllById(servicoIds);
        if (servicos.size() != servicoIds.size()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Um ou mais serviços não encontrados"));
        }
        
        // Calcular valor total
        Double valorTotal = servicos.stream()
            .mapToDouble(Servico::getPreco)
            .sum();
        
        // Criar agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setDataAgendamento(request.getDataAgendamento());
        agendamento.setHorario(request.getHorario());
        agendamento.setMetodoAtendimento(request.getMetodoAtendimento());
        agendamento.setPortePet(request.getPortePet());
        agendamento.setObservacoes(request.getObservacoes());
        agendamento.setValorTotal(valorTotal);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setCliente(cliente.get());
        agendamento.setPet(pet.get());
        agendamento.setServicos(servicos);
        
        Agendamento saved = agendamentoRepository.save(agendamento);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toAgendamentoResponse(saved));
    }

    @PutMapping("/agendamentos/{id}")
    public ResponseEntity<?> updateAgendamento(@PathVariable Long id,
                                               @Valid @RequestBody AgendamentoRequestDTO request) {
        Optional<Agendamento> existingOpt = agendamentoRepository.findById(id);
        
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Agendamento não encontrado"));
        }
        
        Agendamento agendamento = existingOpt.get();
        
        // Validar cliente se fornecido
        if (request.getClienteId() != null) {
            Optional<Cliente> cliente = clienteRepository.findById(request.getClienteId());
            if (cliente.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cliente não encontrado"));
            }
            agendamento.setCliente(cliente.get());
        }
        
        // Validar pet se fornecido
        if (request.getPetId() != null) {
            Optional<Pet> pet = petRepository.findById(request.getPetId());
            if (pet.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Pet não encontrado"));
            }
            agendamento.setPet(pet.get());
        }
        
        // Atualizar serviços se fornecidos
        List<Long> servicoIds = request.getServicoIds();
        if (servicoIds != null && !servicoIds.isEmpty()) {
            List<Servico> servicos = servicoRepository.findAllById(servicoIds);
            if (servicos.size() != servicoIds.size()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Um ou mais serviços não encontrados"));
            }
            agendamento.setServicos(servicos);
            
            // Recalcular valor
            Double valorTotal = servicos.stream()
                .mapToDouble(Servico::getPreco)
                .sum();
            agendamento.setValorTotal(valorTotal);
        }
        
        // Atualizar campos
        if (request.getDataAgendamento() != null) {
            agendamento.setDataAgendamento(request.getDataAgendamento());
        }
        if (request.getHorario() != null) {
            agendamento.setHorario(request.getHorario());
        }
        if (request.getMetodoAtendimento() != null) {
            agendamento.setMetodoAtendimento(request.getMetodoAtendimento());
        }
        if (request.getPortePet() != null) {
            agendamento.setPortePet(request.getPortePet());
        }
        if (request.getObservacoes() != null) {
            agendamento.setObservacoes(request.getObservacoes());
        }
        
        Agendamento updated = agendamentoRepository.save(agendamento);
        
        return ResponseEntity.ok(toAgendamentoResponse(updated));
    }

    @PatchMapping("/agendamentos/{id}/status")
    public ResponseEntity<?> updateAgendamentoStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        Optional<Agendamento> existingOpt = agendamentoRepository.findById(id);
        
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Agendamento não encontrado"));
        }
        
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Status é obrigatório"));
        }
        
        StatusAgendamento newStatus;
        try {
            newStatus = StatusAgendamento.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Status inválido"));
        }
        
        Agendamento agendamento = existingOpt.get();
        agendamento.setStatus(newStatus);
        
        Agendamento updated = agendamentoRepository.save(agendamento);
        
        return ResponseEntity.ok(toAgendamentoResponse(updated));
    }

    @DeleteMapping("/agendamentos/{id}")
    public ResponseEntity<?> deleteAgendamento(@PathVariable Long id) {
        Optional<Agendamento> agendamento = agendamentoRepository.findById(id);
        
        if (agendamento.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Agendamento não encontrado"));
        }
        
        agendamentoRepository.delete(agendamento.get());
        
        return ResponseEntity.ok(Map.of("message", "Agendamento excluído com sucesso"));
    }

    // === HORÁRIOS DISPONÍVEIS ===
    
    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<Map<String, Object>> getHorariosDisponiveis(
            @RequestParam String data) {
        
        LocalDate dataAgendamento = LocalDate.parse(data);
        
        // Horários padrão de funcionamento
        String[] todosHorarios = {"08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"};
        
        // Buscar agendamentos do dia
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findByDataAgendamento(dataAgendamento);
        
        // Filtrar horários ocupados
        List<String> horariosOcupados = agendamentosDoDia.stream()
            .map(a -> a.getHorario().toString().substring(0, 5))
            .collect(Collectors.toList());
        
        List<String> horariosDisponiveis = new java.util.ArrayList<>();
        for (String horario : todosHorarios) {
            if (!horariosOcupados.contains(horario)) {
                horariosDisponiveis.add(horario);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("horariosDisponiveis", horariosDisponiveis);
        response.put("totalDisponiveis", horariosDisponiveis.size());
        
        return ResponseEntity.ok(response);
    }

    // === MÉTODOS AUXILIARES ===
    
    private AgendamentoResponseDTO toAgendamentoResponse(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setDataAgendamento(agendamento.getDataAgendamento());
        dto.setHorario(agendamento.getHorario());
        dto.setMetodoAtendimento(agendamento.getMetodoAtendimento());
        dto.setPortePet(agendamento.getPortePet());
        dto.setObservacoes(agendamento.getObservacoes());
        dto.setValorTotal(agendamento.getValorTotal());
        dto.setStatus(agendamento.getStatus().name());
        
        if (agendamento.getCliente() != null) {
            dto.setClienteId(agendamento.getCliente().getId());
            dto.setClienteNome(agendamento.getCliente().getNome());
        }
        
        if (agendamento.getPet() != null) {
            dto.setPetId(agendamento.getPet().getId());
            dto.setPetNome(agendamento.getPet().getNome());
        }
        
        if (agendamento.getServicos() != null) {
            dto.setServicos(agendamento.getServicos().stream()
                .map(s -> new ServicoSimpleDTO(s.getId(), s.getNome(), s.getPreco()))
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
