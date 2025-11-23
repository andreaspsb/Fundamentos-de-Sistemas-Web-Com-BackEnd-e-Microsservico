package com.petshop.controller;

import com.petshop.dto.PetRequestDTO;
import com.petshop.dto.PetResponseDTO;
import com.petshop.model.Pet;
import com.petshop.service.PetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Gerenciamento de pets cadastrados")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> listarTodos() {
        List<PetResponseDTO> pets = petService.listarTodos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> buscarPorId(@PathVariable Long id) {
        return petService.buscarPorId(id)
                .map(pet -> ResponseEntity.ok(toResponseDTO(pet)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PetResponseDTO>> buscarPorCliente(@PathVariable Long clienteId) {
        List<PetResponseDTO> pets = petService.buscarPorCliente(clienteId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<PetResponseDTO>> buscarPorTipo(@PathVariable String tipo) {
        List<PetResponseDTO> pets = petService.buscarPorTipo(tipo)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pets);
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> criar(@Valid @RequestBody PetRequestDTO dto) {
        Pet pet = toEntity(dto);
        Pet petSalvo = petService.salvar(pet, dto.getClienteId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(petSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO dto) {
        Pet pet = toEntity(dto);
        Pet petAtualizado = petService.atualizar(id, pet);
        return ResponseEntity.ok(toResponseDTO(petAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        petService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos de conversão
    private PetResponseDTO toResponseDTO(Pet pet) {
        return new PetResponseDTO(
                pet.getId(),
                pet.getNome(),
                pet.getTipo(),
                pet.getRaca(),
                pet.getIdade(),
                pet.getPeso(),
                pet.getSexo(),
                pet.getCastrado(),
                pet.getObservacoes(),
                pet.getTemAlergia(),
                pet.getPrecisaMedicacao(),
                pet.getComportamentoAgressivo(),
                pet.getCliente().getId(),
                pet.getCliente().getNome()
        );
    }

    private Pet toEntity(PetRequestDTO dto) {
        Pet pet = new Pet(
                dto.getNome(),
                dto.getTipo(),
                dto.getRaca(),
                dto.getIdade(),
                dto.getPeso(),
                dto.getSexo(),
                dto.getCastrado(),
                dto.getObservacoes()
        );
        pet.setTemAlergia(dto.getTemAlergia());
        pet.setPrecisaMedicacao(dto.getPrecisaMedicacao());
        pet.setComportamentoAgressivo(dto.getComportamentoAgressivo());
        return pet;
    }
}
