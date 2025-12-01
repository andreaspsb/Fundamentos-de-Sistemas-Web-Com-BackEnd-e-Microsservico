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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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

        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        
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

        List<Produto> produtos = produtoRepository.findProdutosDisponiveisPorCategoria(categoriaId);
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

        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        
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
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.getCategoriaId());
                categoriaOpt.ifPresent(produto::setCategoria);
            }

            produto = produtoRepository.save(produto);

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
            Optional<Produto> produtoOpt = produtoRepository.findById(id);
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
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.getCategoriaId());
                categoriaOpt.ifPresent(produto::setCategoria);
            }

            produto = produtoRepository.save(produto);

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
            Optional<Produto> produtoOpt = produtoRepository.findById(id);
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
            Optional<Produto> produtoOpt = produtoRepository.findById(id);
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
            Optional<Produto> produtoOpt = produtoRepository.findById(id);
            if (produtoOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Produto não encontrado"))
                        .build();
            }

            produtoRepository.deleteById(id);

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
