package com.petshop.controller;

import com.petshop.dto.AgendamentoRequestDTO;
import com.petshop.dto.AgendamentoResponseDTO;
import com.petshop.dto.ServicoSimpleDTO;
import com.petshop.model.Agendamento;
import com.petshop.model.Agendamento.StatusAgendamento;
import com.petshop.service.AgendamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agendamentos")
@Tag(name = "Agendamentos", description = "Agendamento de serviços (Banho e Tosa)")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> listarTodos() {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.listarTodos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id)
                .map(agendamento -> ResponseEntity.ok(toResponseDTO(agendamento)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarPorCliente(@PathVariable Long clienteId) {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.buscarPorCliente(clienteId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/data/{data}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarPorData(@PathVariable String data) {
        LocalDate localDate = LocalDate.parse(data);
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.buscarPorData(localDate)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarPorStatus(@PathVariable String status) {
        StatusAgendamento statusEnum = StatusAgendamento.valueOf(status.toUpperCase());
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.buscarPorStatus(statusEnum)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<Boolean> verificarDisponibilidade(
            @RequestParam String data,
            @RequestParam String horario) {
        LocalDate localDate = LocalDate.parse(data);
        LocalTime localTime = LocalTime.parse(horario);
        boolean disponivel = agendamentoService.verificarDisponibilidade(localDate, localTime);
        return ResponseEntity.ok(disponivel);
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> criar(@Valid @RequestBody AgendamentoRequestDTO dto) {
        Agendamento agendamento = new Agendamento(
                dto.getDataAgendamento(),
                dto.getHorario(),
                dto.getMetodoAtendimento(),
                dto.getPortePet(),
                dto.getObservacoes(),
                0.0
        );
        Agendamento agendamentoSalvo = agendamentoService.salvar(
                agendamento,
                dto.getClienteId(),
                dto.getPetId(),
                dto.getServicoIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(agendamentoSalvo));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AgendamentoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        StatusAgendamento statusEnum = StatusAgendamento.valueOf(status.toUpperCase());
        Agendamento agendamento = agendamentoService.atualizarStatus(id, statusEnum);
        return ResponseEntity.ok(toResponseDTO(agendamento));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        agendamentoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos de conversão
    private AgendamentoResponseDTO toResponseDTO(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setDataAgendamento(agendamento.getDataAgendamento());
        dto.setHorario(agendamento.getHorario());
        dto.setMetodoAtendimento(agendamento.getMetodoAtendimento());
        dto.setPortePet(agendamento.getPortePet());
        dto.setObservacoes(agendamento.getObservacoes());
        dto.setValorTotal(agendamento.getValorTotal());
        dto.setStatus(agendamento.getStatus().toString());
        dto.setClienteId(agendamento.getCliente().getId());
        dto.setClienteNome(agendamento.getCliente().getNome());
        dto.setClienteTelefone(agendamento.getCliente().getTelefone());
        dto.setPetId(agendamento.getPet().getId());
        dto.setPetNome(agendamento.getPet().getNome());
        dto.setServicos(agendamento.getServicos().stream()
                .map(s -> new ServicoSimpleDTO(s.getId(), s.getNome(), s.getPreco()))
                .collect(Collectors.toList()));
        return dto;
    }
}
