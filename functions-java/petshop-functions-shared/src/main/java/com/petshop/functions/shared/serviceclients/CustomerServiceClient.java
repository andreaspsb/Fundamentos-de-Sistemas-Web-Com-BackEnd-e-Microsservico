package com.petshop.functions.shared.serviceclients;

import com.petshop.functions.shared.dto.ClienteResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Client para comunicação com o serviço de Clientes
 */
@Component
public class CustomerServiceClient extends BaseServiceClient {

    private final String baseUrl;

    public CustomerServiceClient(
            ResilienceFactory resilienceFactory,
            @Value("${services.customers.url:http://localhost:7082}") String baseUrl) {
        super("customer-service", baseUrl, resilienceFactory);
        this.baseUrl = baseUrl;
    }

    /**
     * Busca um cliente por ID
     */
    public Optional<ClienteResponseDTO> getCustomerById(Long clienteId, String token) {
        return executeWithResilience(() -> {
            try {
                HttpResponse<String> response = doGet(baseUrl + "/api/clientes/" + clienteId, token);
                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), ClienteResponseDTO.class);
                }
                return null;
            } catch (Exception e) {
                logger.error("Erro ao buscar cliente {}: {}", clienteId, e.getMessage());
                return null;
            }
        }, null);
    }

    /**
     * Verifica se um cliente existe
     */
    public boolean customerExists(Long clienteId, String token) {
        try {
            HttpResponse<String> response = doGet(baseUrl + "/api/clientes/" + clienteId, token);
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.error("Erro ao verificar cliente {}: {}", clienteId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca cliente por CPF
     */
    public Optional<ClienteResponseDTO> getCustomerByCpf(String cpf, String token) {
        return executeWithResilience(() -> {
            try {
                HttpResponse<String> response = doGet(baseUrl + "/api/clientes/cpf/" + cpf, token);
                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), ClienteResponseDTO.class);
                }
                return null;
            } catch (Exception e) {
                logger.error("Erro ao buscar cliente por CPF {}: {}", cpf, e.getMessage());
                return null;
            }
        }, null);
    }

    /**
     * Busca cliente por email
     */
    public Optional<ClienteResponseDTO> getCustomerByEmail(String email, String token) {
        return executeWithResilience(() -> {
            try {
                HttpResponse<String> response = doGet(baseUrl + "/api/clientes/email/" + email, token);
                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), ClienteResponseDTO.class);
                }
                return null;
            } catch (Exception e) {
                logger.error("Erro ao buscar cliente por email {}: {}", email, e.getMessage());
                return null;
            }
        }, null);
    }
}
