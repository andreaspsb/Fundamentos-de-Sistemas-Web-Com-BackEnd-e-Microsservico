package com.petshop.functions.shared.serviceclients;

import com.petshop.functions.shared.dto.ProdutoResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Client para comunicação com o serviço de Catálogo
 */
@Component
public class CatalogServiceClient extends BaseServiceClient {

    private final String baseUrl;

    public CatalogServiceClient(
            ResilienceFactory resilienceFactory,
            @Value("${services.catalog.url:http://localhost:7084}") String baseUrl) {
        super("catalog-service", baseUrl, resilienceFactory);
        this.baseUrl = baseUrl;
    }

    /**
     * Busca um produto por ID
     */
    public Optional<ProdutoResponseDTO> getProductById(Long produtoId) {
        return executeWithResilience(() -> {
            try {
                HttpResponse<String> response = doGet(baseUrl + "/api/produtos/" + produtoId, null);
                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), ProdutoResponseDTO.class);
                }
                return null;
            } catch (Exception e) {
                logger.error("Erro ao buscar produto {}: {}", produtoId, e.getMessage());
                return null;
            }
        }, null);
    }

    /**
     * Busca todos os produtos
     */
    public List<ProdutoResponseDTO> getAllProducts() {
        try {
            HttpResponse<String> response = doGet(baseUrl + "/api/produtos", null);
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ProdutoResponseDTO.class));
            }
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Erro ao buscar produtos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Verifica estoque de um produto
     */
    public Optional<Integer> getProductStock(Long produtoId) {
        return executeWithResilience(() -> {
            try {
                HttpResponse<String> response = doGet(baseUrl + "/api/produtos/" + produtoId + "/estoque", null);
                if (response.statusCode() == 200) {
                    return Integer.parseInt(response.body());
                }
                return null;
            } catch (Exception e) {
                logger.error("Erro ao verificar estoque do produto {}: {}", produtoId, e.getMessage());
                return null;
            }
        }, null);
    }

    /**
     * Atualiza estoque de um produto (requer token admin)
     */
    public boolean updateStock(Long produtoId, int quantidade, String token) {
        try {
            record StockUpdate(int quantidade) {}
            HttpResponse<String> response = doPut(
                    baseUrl + "/api/produtos/" + produtoId + "/estoque",
                    new StockUpdate(quantidade),
                    token
            );
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.error("Erro ao atualizar estoque do produto {}: {}", produtoId, e.getMessage());
            return false;
        }
    }

    /**
     * Deduz quantidade do estoque
     */
    public boolean deductStock(Long produtoId, int quantidade, String token) {
        try {
            record StockDeduction(int quantidade) {}
            HttpResponse<String> response = doPost(
                    baseUrl + "/api/produtos/" + produtoId + "/deduzir-estoque",
                    new StockDeduction(quantidade),
                    token
            );
            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.error("Erro ao deduzir estoque do produto {}: {}", produtoId, e.getMessage());
            return false;
        }
    }
}
