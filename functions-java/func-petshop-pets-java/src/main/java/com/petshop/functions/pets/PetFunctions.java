package com.petshop.functions.pets;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.PetRequestDTO;
import com.petshop.functions.shared.dto.PetResponseDTO;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.model.Pet;
import com.petshop.functions.shared.repository.ClienteRepository;
import com.petshop.functions.shared.repository.PetRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Azure Functions for Pet Management
 */
@Component
public class PetFunctions {

    private final PetRepository petRepository;
    private final ClienteRepository clienteRepository;
    private final FunctionAuthorization functionAuthorization;

    @Autowired
    public PetFunctions(
            PetRepository petRepository,
            ClienteRepository clienteRepository,
            FunctionAuthorization functionAuthorization) {
        this.petRepository = petRepository;
        this.clienteRepository = clienteRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/pets
     * List all pets (Admin) or user's pets (Cliente)
     */
    @FunctionName("getAllPets")
    public HttpResponseMessage getAllPets(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pets"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all pets");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            List<Pet> pets;
            
            if ("Admin".equals(authResult.role())) {
                pets = petRepository.findAll();
            } else {
                // Cliente sees only their pets
                if (authResult.clienteId() == null) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(List.of())
                            .build();
                }
                pets = petRepository.findByClienteId(authResult.clienteId());
            }

            List<PetResponseDTO> response = pets.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/pets/{id}
     * Get pet by ID
     */
    @FunctionName("getPetById")
    public HttpResponseMessage getPetById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pets/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting pet by ID: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Pet> petOpt = petRepository.findById(id);
            
            if (petOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pet não encontrado"))
                        .build();
            }

            Pet pet = petOpt.get();

            // Cliente can only see their own pets
            if ("Cliente".equals(authResult.role()) && 
                (pet.getCliente() == null || !pet.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode visualizar seus próprios pets");
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(pet))
                    .build();
        });
    }

    /**
     * GET /api/pets/cliente/{clienteId}
     * Get pets by customer ID
     */
    @FunctionName("getPetsByCustomerId")
    public HttpResponseMessage getPetsByCustomerId(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pets/cliente/{clienteId}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("clienteId") Long clienteId,
            final ExecutionContext context) {

        context.getLogger().info("Getting pets by customer ID: " + clienteId);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            // Cliente can only see their own pets
            if ("Cliente".equals(authResult.role()) && !clienteId.equals(authResult.clienteId())) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode visualizar seus próprios pets");
            }

            List<Pet> pets = petRepository.findByClienteId(clienteId);
            List<PetResponseDTO> response = pets.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * POST /api/pets
     * Create new pet
     */
    @FunctionName("createPet")
    public HttpResponseMessage createPet(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pets"
            ) HttpRequestMessage<Optional<PetRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new pet");

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<PetRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            PetRequestDTO dto = bodyOpt.get();

            // Validate required fields
            if (dto.getNome() == null || dto.getTipo() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Nome e tipo são obrigatórios"))
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

            Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
            if (clienteOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Cliente não encontrado"))
                        .build();
            }

            Pet pet = new Pet();
            pet.setNome(dto.getNome());
            pet.setTipo(dto.getTipo());
            pet.setRaca(dto.getRaca());
            pet.setIdade(dto.getIdade());
            pet.setPeso(dto.getPeso());
            pet.setCliente(clienteOpt.get());

            pet = petRepository.save(pet);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(pet))
                    .build();
        });
    }

    /**
     * PUT /api/pets/{id}
     * Update pet
     */
    @FunctionName("updatePet")
    public HttpResponseMessage updatePet(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pets/{id}"
            ) HttpRequestMessage<Optional<PetRequestDTO>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating pet: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Pet> petOpt = petRepository.findById(id);
            if (petOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pet não encontrado"))
                        .build();
            }

            Pet pet = petOpt.get();

            // Cliente can only update their own pets
            if ("Cliente".equals(authResult.role()) && 
                (pet.getCliente() == null || !pet.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode atualizar seus próprios pets");
            }

            Optional<PetRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            PetRequestDTO dto = bodyOpt.get();

            // Update fields
            if (dto.getNome() != null) pet.setNome(dto.getNome());
            if (dto.getTipo() != null) pet.setTipo(dto.getTipo());
            if (dto.getRaca() != null) pet.setRaca(dto.getRaca());
            if (dto.getIdade() != null) pet.setIdade(dto.getIdade());
            if (dto.getPeso() != null) pet.setPeso(dto.getPeso());

            // Admin can change owner
            if ("Admin".equals(authResult.role()) && dto.getClienteId() != null) {
                Optional<Cliente> clienteOpt = clienteRepository.findById(dto.getClienteId());
                clienteOpt.ifPresent(pet::setCliente);
            }

            pet = petRepository.save(pet);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(pet))
                    .build();
        });
    }

    /**
     * DELETE /api/pets/{id}
     * Delete pet
     */
    @FunctionName("deletePet")
    public HttpResponseMessage deletePet(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "pets/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting pet: " + id);

        return functionAuthorization.executeProtectedWithRoles(request, Set.of("Admin", "Cliente"), authResult -> {
            Optional<Pet> petOpt = petRepository.findById(id);
            if (petOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Pet não encontrado"))
                        .build();
            }

            Pet pet = petOpt.get();

            // Cliente can only delete their own pets
            if ("Cliente".equals(authResult.role()) && 
                (pet.getCliente() == null || !pet.getCliente().getId().equals(authResult.clienteId()))) {
                return functionAuthorization.createForbiddenResponse(request, "Você só pode excluir seus próprios pets");
            }

            petRepository.deleteById(id);

            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .build();
        });
    }

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
                pet.getCliente() != null ? pet.getCliente().getId() : null,
                pet.getCliente() != null ? pet.getCliente().getNome() : null
        );
    }
}
