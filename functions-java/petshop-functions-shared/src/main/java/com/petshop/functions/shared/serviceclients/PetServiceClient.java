package com.petshop.functions.shared.serviceclients;

import com.petshop.functions.shared.dto.PetResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Client para comunicação com o serviço de Pets
 */
@Component
public class PetServiceClient extends BaseServiceClient {

    private final String baseUrl;

    public PetServiceClient(
            ResilienceFactory resilienceFactory,
            @Value("${services.pets.url:http://localhost:7083}") String baseUrl) {
        super("pet-service", baseUrl, resilienceFactory);
        this.baseUrl = baseUrl;
    }

    /**
     * Busca um pet por ID
     */
    public Optional<PetResponseDTO> getPetById(Long petId, String token) {
        return executeWithResilience(() -> {
            try {
                HttpResponse<String> response = doGet(baseUrl + "/api/pets/" + petId, token);
                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), PetResponseDTO.class);
                }
                return null;
            } catch (Exception e) {
                logger.error("Erro ao buscar pet {}: {}", petId, e.getMessage());
                return null;
            }
        }, null);
    }

    /**
     * Verifica se um pet existe
     */
    public boolean petExists(Long petId, String token) {
        try {
            HttpResponse<String> response = doGet(baseUrl + "/api/pets/" + petId, token);
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.error("Erro ao verificar pet {}: {}", petId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca pets de um cliente
     */
    public List<PetResponseDTO> getPetsByCustomerId(Long clienteId, String token) {
        try {
            HttpResponse<String> response = doGet(baseUrl + "/api/pets/cliente/" + clienteId, token);
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, PetResponseDTO.class));
            }
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Erro ao buscar pets do cliente {}: {}", clienteId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Verifica se o pet pertence ao cliente
     */
    public boolean isPetOwnedByCustomer(Long petId, Long clienteId, String token) {
        try {
            Optional<PetResponseDTO> petOpt = getPetById(petId, token);
            return petOpt.map(pet -> clienteId.equals(pet.getClienteId())).orElse(false);
        } catch (Exception e) {
            logger.error("Erro ao verificar dono do pet {}: {}", petId, e.getMessage());
            return false;
        }
    }
}
