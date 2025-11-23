package com.petshop.controller;

import com.petshop.dto.ServicoRequestDTO;
import com.petshop.dto.ServicoResponseDTO;
import com.petshop.model.Servico;
import com.petshop.service.ServicoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servicos")
@Tag(name = "Serviços", description = "Gerenciamento de serviços disponíveis (Banho, Tosa, etc)")
public class ServicoController {

    @Autowired
    private ServicoService servicoService;

    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarTodos() {
        List<ServicoResponseDTO> servicos = servicoService.listarTodos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ServicoResponseDTO>> listarAtivos() {
        List<ServicoResponseDTO> servicos = servicoService.listarAtivos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id)
                .map(servico -> ResponseEntity.ok(toResponseDTO(servico)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> criar(@Valid @RequestBody ServicoRequestDTO dto) {
        Servico servico = toEntity(dto);
        Servico servicoSalvo = servicoService.salvar(servico);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(servicoSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequestDTO dto) {
        Servico servico = toEntity(dto);
        Servico servicoAtualizado = servicoService.atualizar(id, servico);
        return ResponseEntity.ok(toResponseDTO(servicoAtualizado));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        servicoService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        servicoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos de conversão
    private ServicoResponseDTO toResponseDTO(Servico servico) {
        return new ServicoResponseDTO(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco(),
                servico.getAtivo()
        );
    }

    private Servico toEntity(ServicoRequestDTO dto) {
        Servico servico = new Servico(
                dto.getNome(),
                dto.getDescricao(),
                dto.getPreco()
        );
        servico.setAtivo(dto.getAtivo());
        return servico;
    }
}
