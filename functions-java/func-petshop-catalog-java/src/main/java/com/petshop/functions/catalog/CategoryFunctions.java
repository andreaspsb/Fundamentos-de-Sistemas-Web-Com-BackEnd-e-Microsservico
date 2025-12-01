package com.petshop.functions.catalog;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.petshop.functions.shared.dto.CategoriaRequestDTO;
import com.petshop.functions.shared.dto.CategoriaResponseDTO;
import com.petshop.functions.shared.model.Categoria;
import com.petshop.functions.shared.repository.CategoriaRepository;
import com.petshop.functions.shared.security.FunctionAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Azure Functions for Category Management
 */
@Component
public class CategoryFunctions {

    private final CategoriaRepository categoriaRepository;
    private final FunctionAuthorization functionAuthorization;

    @Autowired
    public CategoryFunctions(
            CategoriaRepository categoriaRepository,
            FunctionAuthorization functionAuthorization) {
        this.categoriaRepository = categoriaRepository;
        this.functionAuthorization = functionAuthorization;
    }

    /**
     * GET /api/categorias
     * List all active categories (public)
     */
    @FunctionName("getAllCategories")
    public HttpResponseMessage getAllCategories(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "categorias"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all categories");

        List<Categoria> categorias = categoriaRepository.findByAtivo(true);
        List<CategoriaResponseDTO> response = categorias.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }

    /**
     * GET /api/categorias/all
     * List all categories including inactive (Admin only)
     */
    @FunctionName("getAllCategoriesAdmin")
    public HttpResponseMessage getAllCategoriesAdmin(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "categorias/all"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all categories (admin)");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            List<Categoria> categorias = categoriaRepository.findAll();
            List<CategoriaResponseDTO> response = categorias.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(response)
                    .build();
        });
    }

    /**
     * GET /api/categorias/{id}
     * Get category by ID (public)
     */
    @FunctionName("getCategoryById")
    public HttpResponseMessage getCategoryById(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "categorias/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Getting category by ID: " + id);

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        
        if (categoriaOpt.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body(Map.of("error", "Categoria não encontrada"))
                    .build();
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(toResponseDTO(categoriaOpt.get()))
                .build();
    }

    /**
     * POST /api/categorias
     * Create new category (Admin only)
     */
    @FunctionName("createCategory")
    public HttpResponseMessage createCategory(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "categorias"
            ) HttpRequestMessage<Optional<CategoriaRequestDTO>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new category");

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<CategoriaRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            CategoriaRequestDTO dto = bodyOpt.get();

            if (dto.getNome() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Nome é obrigatório"))
                        .build();
            }

            if (categoriaRepository.existsByNome(dto.getNome())) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Categoria já existe"))
                        .build();
            }

            Categoria categoria = new Categoria();
            categoria.setNome(dto.getNome());
            categoria.setDescricao(dto.getDescricao());
            categoria.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

            categoria = categoriaRepository.save(categoria);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(categoria))
                    .build();
        });
    }

    /**
     * PUT /api/categorias/{id}
     * Update category (Admin only)
     */
    @FunctionName("updateCategory")
    public HttpResponseMessage updateCategory(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "categorias/{id}"
            ) HttpRequestMessage<Optional<CategoriaRequestDTO>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Updating category: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
            if (categoriaOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Categoria não encontrada"))
                        .build();
            }

            Optional<CategoriaRequestDTO> bodyOpt = request.getBody();
            if (bodyOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Request body is required"))
                        .build();
            }

            CategoriaRequestDTO dto = bodyOpt.get();
            Categoria categoria = categoriaOpt.get();

            if (dto.getNome() != null) categoria.setNome(dto.getNome());
            if (dto.getDescricao() != null) categoria.setDescricao(dto.getDescricao());
            if (dto.getAtivo() != null) categoria.setAtivo(dto.getAtivo());

            categoria = categoriaRepository.save(categoria);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(toResponseDTO(categoria))
                    .build();
        });
    }

    /**
     * DELETE /api/categorias/{id}
     * Delete category (Admin only)
     */
    @FunctionName("deleteCategory")
    public HttpResponseMessage deleteCategory(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "categorias/{id}"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting category: " + id);

        return functionAuthorization.executeProtectedAdmin(request, authResult -> {
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
            if (categoriaOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body(Map.of("error", "Categoria não encontrada"))
                        .build();
            }

            categoriaRepository.deleteById(id);

            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .build();
        });
    }

    private CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getAtivo()
        );
    }
}
