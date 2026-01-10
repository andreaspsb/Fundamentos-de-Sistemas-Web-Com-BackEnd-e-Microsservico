package com.petshop.functions.catalog;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.ProdutoRequestDTO;
import com.petshop.functions.shared.dto.ProdutoResponseDTO;
import com.petshop.functions.shared.model.Categoria;
import com.petshop.functions.shared.model.Produto;
import com.petshop.functions.shared.repository.CategoriaRepository;
import com.petshop.functions.shared.repository.ProdutoRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import com.petshop.shared.util.ValidationUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Azure Functions for Product Management
 */
@Component
public class ProductFunctions {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FunctionAuthorization functionAuthorization;

    public ProductFunctions(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            FunctionAuthorization functionAuthorization) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/produtos
     * List all available products (public)
     */
    @FunctionName("getAllProducts")
    public HttpResponseMessage getAllProducts(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all products");

        List<Produto> produtos = produtoRepository.findProdutosDisponiveis();
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/produtos/all
     * List all products including unavailable (Admin only)
     */
    @FunctionName("getAllProductsAdmin")
    public HttpResponseMessage getAllProductsAdmin(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/all"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all products (admin)");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            List<Produto> produtos = produtoRepository.findAll();
            List<ProdutoResponseDTO> response = produtos.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/produtos/disponiveis
     * List available products only (public)
     */
    @FunctionName("getAvailableProducts")
    public HttpResponseMessage getAvailableProducts(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/disponiveis"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting available products");

        List<Produto> produtos = produtoRepository.findProdutosDisponiveis();
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/produtos/{id}
     * Get product by ID (public)
     */
    @FunctionName("getProductById")
    public HttpResponseMessage getProductById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting product by ID: " + id);

        Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
        
        if (produtoOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Produto não encontrado"))
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(toResponseDTO(produtoOpt.get()))
                .build();
    }

    /**
     * GET /api/produtos/categoria/{categoriaId}
     * Get products by category (public)
     */
    @FunctionName("getProductsByCategory")
    public HttpResponseMessage getProductsByCategory(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/categoria/{categoriaId}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("categoriaId") Long categoriaId,
            final ExecutionContext context) {

        context.getLogger().info("Getting products by category: " + categoriaId);

        List<Produto> produtos = produtoRepository.findProdutosDisponiveisPorCategoria(ValidationUtils.requireNonNullId(categoriaId, "Categoria"));
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/produtos/categoria/{categoriaId}/disponiveis
     * Get available products by category (public)
     */
    @FunctionName("getAvailableProductsByCategory")
    public HttpResponseMessage getAvailableProductsByCategory(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/categoria/{categoriaId}/disponiveis"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("categoriaId") Long categoriaId,
            final ExecutionContext context) {

        context.getLogger().info("Getting available products by category: " + categoriaId);

        List<Produto> produtos = produtoRepository.findProdutosDisponiveisPorCategoria(ValidationUtils.requireNonNullId(categoriaId, "Categoria"));
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/produtos/buscar
     * Search products by name (public)
     */
    @FunctionName("searchProducts")
    public HttpResponseMessage searchProducts(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/buscar"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Searching products");

        String nome = request.getQueryParameters().get("nome");
        if (nome == null || nome.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Parâmetro 'nome' é obrigatório"))
                    .build();
        }

        List<Produto> produtos = produtoRepository.buscarPorNome(nome);
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/produtos/estoque-baixo
     * Get products with low stock (Admin only)
     */
    @FunctionName("getLowStockProducts")
    public HttpResponseMessage getLowStockProducts(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/estoque-baixo"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting low stock products");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            int threshold = 10; // Default threshold
            String thresholdParam = request.getQueryParameters().get("quantidade");
            if (thresholdParam != null) {
                try {
                    threshold = Integer.parseInt(thresholdParam);
                } catch (NumberFormatException ignored) {
                    // Use default threshold
                }
            }

            List<Produto> produtos = produtoRepository.findByQuantidadeEstoqueLessThan(threshold);
            List<ProdutoResponseDTO> response = produtos.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/produtos/{id}/verificar-estoque
     * Check product stock availability (public)
     */
    @FunctionName("checkProductStock")
    public HttpResponseMessage checkProductStock(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/verificar-estoque"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Checking product stock: " + id);

        String quantidadeParam = request.getQueryParameters().get("quantidade");
        int quantidadeDesejada = 1;
        if (quantidadeParam != null) {
            try {
                quantidadeDesejada = Integer.parseInt(quantidadeParam);
            } catch (NumberFormatException ignored) {
                // Use default
            }
        }

        Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
        
        if (produtoOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Produto não encontrado"))
                    .build();
        }

        Produto produto = produtoOpt.get();
        boolean disponivel = produto.getAtivo() && produto.getQuantidadeEstoque() >= quantidadeDesejada;

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "disponivel", disponivel,
                        "estoqueAtual", produto.getQuantidadeEstoque(),
                        "quantidadeSolicitada", quantidadeDesejada
                ))
                .build();
    }

    /**
     * GET /api/produtos/{id}/estoque
     * Get product stock (public)
     */
    @FunctionName("getProductStock")
    public HttpResponseMessage getProductStock(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/estoque"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting product stock: " + id);

        Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
        
        if (produtoOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Produto não encontrado"))
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "text/plain")
                .body(String.valueOf(produtoOpt.get().getQuantidadeEstoque()))
                .build();
    }

    /**
     * POST /api/produtos
     * Create new product (Admin only)
     */
    @FunctionName("createProduct")
    public HttpResponseMessage createProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos"
            ) HttpRequestMessage<Optional<ProdutoRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new product");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<ProdutoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            ProdutoRequestDTO dto = bodyOpt.get();

            if (dto.getNome() == null || dto.getPreco() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Nome e preço são obrigatórios"))
                        .build();
            }

            Produto produto = new Produto();
            produto.setNome(dto.getNome());
            produto.setDescricao(dto.getDescricao());
            produto.setPreco(dto.getPreco());
            produto.setQuantidadeEstoque(dto.getQuantidadeEstoque() != null ? dto.getQuantidadeEstoque() : 0);
            produto.setUrlImagem(dto.getUrlImagem());
            produto.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

            if (dto.getCategoriaId() != null) {
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(ValidationUtils.requireNonNullId(dto.getCategoriaId(), "Categoria"));
                categoriaOpt.ifPresent(produto::setCategoria);
            }

            produto = ValidationUtils.requireNonNullEntity(produtoRepository.save(produto), "Produto");

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(produto))
                    .build();
        });
    }

    /**
     * PUT /api/produtos/{id}
     * Update product (Admin only)
     */
    @FunctionName("updateProduct")
    public HttpResponseMessage updateProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}"
            ) HttpRequestMessage<Optional<ProdutoRequestDTO>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating product: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            Optional<ProdutoRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            ProdutoRequestDTO dto = bodyOpt.get();
            Produto produto = produtoOpt.get();

            if (dto.getNome() != null) produto.setNome(dto.getNome());
            if (dto.getDescricao() != null) produto.setDescricao(dto.getDescricao());
            if (dto.getPreco() != null) produto.setPreco(dto.getPreco());
            if (dto.getQuantidadeEstoque() != null) produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
            if (dto.getUrlImagem() != null) produto.setUrlImagem(dto.getUrlImagem());
            if (dto.getAtivo() != null) produto.setAtivo(dto.getAtivo());

            if (dto.getCategoriaId() != null) {
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(ValidationUtils.requireNonNullId(dto.getCategoriaId(), "Categoria"));
                categoriaOpt.ifPresent(produto::setCategoria);
            }

            Produto toSave = ValidationUtils.requireNonNullEntity(produto, "Produto");
            produto = ValidationUtils.requireNonNullEntity(produtoRepository.save(toSave), "Produto");

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(produto))
                    .build();
        });
    }

    /**
     * PUT /api/produtos/{id}/estoque
     * Update product stock (Admin only)
     */
    @FunctionName("updateProductStock")
    public HttpResponseMessage updateProductStock(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/estoque"
            ) HttpRequestMessage<Optional<Map<String, Integer>>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating product stock: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            Optional<Map<String, Integer>> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty() || !bodyOpt.get().containsKey("quantidade")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "quantidade é obrigatório"))
                        .build();
            }

            Produto produto = produtoOpt.get();
            produto.setQuantidadeEstoque(bodyOpt.get().get("quantidade"));
            produtoRepository.save(produto);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of("estoque", produto.getQuantidadeEstoque()))
                    .build();
        });
    }

    /**
     * POST /api/produtos/{id}/deduzir-estoque
     * Deduct stock from product (internal use)
     */
    @FunctionName("deductProductStock")
    public HttpResponseMessage deductProductStock(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/deduzir-estoque"
            ) HttpRequestMessage<Optional<Map<String, Integer>>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deducting product stock: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            Optional<Map<String, Integer>> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty() || !bodyOpt.get().containsKey("quantidade")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "quantidade é obrigatório"))
                        .build();
            }

            int quantidade = bodyOpt.get().get("quantidade");
            Produto produto = produtoOpt.get();

            if (produto.getQuantidadeEstoque() < quantidade) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Estoque insuficiente"))
                        .build();
            }

            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
            produtoRepository.save(produto);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of("estoque", produto.getQuantidadeEstoque()))
                    .build();
        });
    }

    /**
     * PATCH /api/produtos/{id}/adicionar-estoque
     * Add stock to product (Admin only)
     */
    @FunctionName("addProductStock")
    public HttpResponseMessage addProductStock(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PATCH},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/adicionar-estoque"
            ) HttpRequestMessage<Optional<Map<String, Integer>>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Adding product stock: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            Optional<Map<String, Integer>> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty() || !bodyOpt.get().containsKey("quantidade")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "quantidade é obrigatório"))
                        .build();
            }

            int quantidade = bodyOpt.get().get("quantidade");
            Produto produto = produtoOpt.get();

            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidade);
            produtoRepository.save(produto);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(produto))
                    .build();
        });
    }

    /**
     * PATCH /api/produtos/{id}/ativar
     * Activate product (Admin only)
     */
    @FunctionName("activateProduct")
    public HttpResponseMessage activateProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PATCH},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/ativar"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Activating product: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            Produto produto = produtoOpt.get();
            produto.setAtivo(true);
            produtoRepository.save(produto);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(produto))
                    .build();
        });
    }

    /**
     * PATCH /api/produtos/{id}/desativar
     * Deactivate product (Admin only)
     */
    @FunctionName("deactivateProduct")
    public HttpResponseMessage deactivateProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PATCH},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}/desativar"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deactivating product: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Produto> produtoOpt = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            Produto produto = produtoOpt.get();
            produto.setAtivo(false);
            produtoRepository.save(produto);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(produto))
                    .build();
        });
    }

    /**
     * DELETE /api/produtos/{id}
     * Delete product (Admin only)
     */
    @FunctionName("deleteProduct")
    public HttpResponseMessage deleteProduct(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "produtos/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting product: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Long safeId = ValidationUtils.requireNonNullId(id, "Produto");
            Optional<Produto> produtoOpt = produtoRepository.findById(safeId);
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            produtoRepository.deleteById(safeId);

            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .build();
        });
    }

    private ProdutoResponseDTO toResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getUrlImagem(),
                produto.getAtivo(),
                produto.getCategoria() != null ? produto.getCategoria().getId() : null,
                produto.getCategoria() != null ? produto.getCategoria().getNome() : null
        );
    }
}
