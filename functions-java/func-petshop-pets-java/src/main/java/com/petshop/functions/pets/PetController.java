package com.petshop.functions.pets;

import com.petshop.functions.shared.dto.*;
import com.petshop.functions.shared.model.Pet;
import com.petshop.functions.shared.repository.PetRepository;
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
 * REST Controller for Pet Management (Docker deployment)
 */
@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "pets");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> getAllPets() {
        List<Pet> pets = petRepository.findAll();
        List<PetResponseDTO> response = pets.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPetById(@PathVariable Long id) {
        Optional<Pet> petOpt = petRepository.findById(id);
        if (petOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Pet not found"));
        }
        return ResponseEntity.ok(toResponseDTO(petOpt.get()));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByCliente(@PathVariable Long clienteId) {
        List<Pet> pets = petRepository.findByClienteId(clienteId);
        List<PetResponseDTO> response = pets.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createPet(@RequestBody PetRequestDTO request) {
        try {
            if (request.getNome() == null || request.getClienteId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Name and clienteId are required"));
            }

            Optional<com.petshop.functions.shared.model.Cliente> clienteOpt = clienteRepository.findById(request.getClienteId());
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Cliente not found"));
            }

            Pet pet = new Pet();
            pet.setNome(request.getNome());
            pet.setTipo(request.getTipo());
            pet.setRaca(request.getRaca());
            pet.setIdade(request.getIdade());
            pet.setPeso(request.getPeso());
            pet.setSexo(request.getSexo());
            pet.setCastrado(request.getCastrado());
            pet.setObservacoes(request.getObservacoes());
            pet.setCliente(clienteOpt.get());

            pet = petRepository.save(pet);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(pet));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create pet: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(@PathVariable Long id, @RequestBody PetRequestDTO request) {
        try {
            Optional<Pet> petOpt = petRepository.findById(id);
            if (petOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Pet not found"));
            }

            Pet pet = petOpt.get();
            
            if (request.getNome() != null) pet.setNome(request.getNome());
            if (request.getTipo() != null) pet.setTipo(request.getTipo());
            if (request.getRaca() != null) pet.setRaca(request.getRaca());
            if (request.getIdade() != null) pet.setIdade(request.getIdade());
            if (request.getPeso() != null) pet.setPeso(request.getPeso());
            if (request.getSexo() != null) pet.setSexo(request.getSexo());
            if (request.getCastrado() != null) pet.setCastrado(request.getCastrado());
            if (request.getObservacoes() != null) pet.setObservacoes(request.getObservacoes());

            pet = petRepository.save(pet);
            return ResponseEntity.ok(toResponseDTO(pet));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update pet: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id) {
        try {
            if (!petRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Pet not found"));
            }
            
            petRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Pet deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete pet: " + e.getMessage()));
        }
    }

    private PetResponseDTO toResponseDTO(Pet pet) {
        PetResponseDTO dto = new PetResponseDTO();
        dto.setId(pet.getId());
        dto.setNome(pet.getNome());
        dto.setTipo(pet.getTipo());
        dto.setRaca(pet.getRaca());
        dto.setIdade(pet.getIdade());
        dto.setPeso(pet.getPeso());
        dto.setSexo(pet.getSexo());
        dto.setCastrado(pet.getCastrado());
        dto.setObservacoes(pet.getObservacoes());
        dto.setTemAlergia(pet.getTemAlergia());
        dto.setPrecisaMedicacao(pet.getPrecisaMedicacao());
        dto.setComportamentoAgressivo(pet.getComportamentoAgressivo());
        if (pet.getCliente() != null) {
            dto.setClienteId(pet.getCliente().getId());
            dto.setClienteNome(pet.getCliente().getNome());
        }
        return dto;
    }
}
