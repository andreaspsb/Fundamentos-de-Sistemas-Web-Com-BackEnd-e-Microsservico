package com.petshop.functions.catalog;

import com.petshop.functions.shared.dto.*;
import com.petshop.functions.shared.model.Categoria;
import com.petshop.functions.shared.model.Produto;
import com.petshop.functions.shared.model.Servico;
import com.petshop.functions.shared.repository.CategoriaRepository;
import com.petshop.functions.shared.repository.ProdutoRepository;
import com.petshop.functions.shared.repository.ServicoRepository;
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
 * REST Controller for Catalog Management (Docker deployment)
 * Handles Categories, Products, and Services
 */
@RestController
@CrossOrigin(origins = "*")
public class CatalogController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    // ==================== HEALTH ====================

    @GetMapping("/api/categories/health")
    public ResponseEntity<Map<String, Object>> healthCategories() {
        return health("categories");
    }

    @GetMapping("/api/products/health")
    public ResponseEntity<Map<String, Object>> healthProducts() {
        return health("products");
    }

    @GetMapping("/api/services/health")
    public ResponseEntity<Map<String, Object>> healthServices() {
        return health("services");
    }

    private ResponseEntity<Map<String, Object>> health(String service) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "catalog-" + service);
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // ==================== CATEGORIES ====================

    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoriaResponseDTO>> getAllCategories() {
        List<Categoria> categorias = categoriaRepository.findAll();
        List<CategoriaResponseDTO> response = categorias.stream()
                .map(this::toCategoriaResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        if (categoriaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Category not found"));
        }
        return ResponseEntity.ok(toCategoriaResponseDTO(categoriaOpt.get()));
    }

    @PostMapping("/api/categories")
    public ResponseEntity<?> createCategory(@RequestBody CategoriaRequestDTO request) {
        try {
            Categoria categoria = new Categoria();
            categoria.setNome(request.getNome());
            categoria.setDescricao(request.getDescricao());
            categoria = categoriaRepository.save(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(toCategoriaResponseDTO(categoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create category: " + e.getMessage()));
        }
    }

    @PutMapping("/api/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoriaRequestDTO request) {
        try {
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
            if (categoriaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Category not found"));
            }
            Categoria categoria = categoriaOpt.get();
            if (request.getNome() != null) categoria.setNome(request.getNome());
            if (request.getDescricao() != null) categoria.setDescricao(request.getDescricao());
            categoria = categoriaRepository.save(categoria);
            return ResponseEntity.ok(toCategoriaResponseDTO(categoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update category: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            if (!categoriaRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Category not found"));
            }
            categoriaRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete category: " + e.getMessage()));
        }
    }

    // ==================== PRODUCTS ====================

    @GetMapping("/api/products")
    public ResponseEntity<List<ProdutoResponseDTO>> getAllProducts() {
        List<Produto> produtos = produtoRepository.findAll();
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toProdutoResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found"));
        }
        return ResponseEntity.ok(toProdutoResponseDTO(produtoOpt.get()));
    }

    @GetMapping("/api/products/categoria/{categoriaId}")
    public ResponseEntity<List<ProdutoResponseDTO>> getProductsByCategory(@PathVariable Long categoriaId) {
        List<Produto> produtos = produtoRepository.findByCategoriaId(categoriaId);
        List<ProdutoResponseDTO> response = produtos.stream()
                .map(this::toProdutoResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/products")
    public ResponseEntity<?> createProduct(@RequestBody ProdutoRequestDTO request) {
        try {
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(request.getCategoriaId());
            if (categoriaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Category not found"));
            }

            Produto produto = new Produto();
            produto.setNome(request.getNome());
            produto.setDescricao(request.getDescricao());
            produto.setPreco(request.getPreco());
            produto.setQuantidadeEstoque(request.getQuantidadeEstoque());
            produto.setCategoria(categoriaOpt.get());
            produto.setUrlImagem(request.getUrlImagem());
            produto.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);
            produto = produtoRepository.save(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toProdutoResponseDTO(produto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create product: " + e.getMessage()));
        }
    }

    @PutMapping("/api/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProdutoRequestDTO request) {
        try {
            Optional<Produto> produtoOpt = produtoRepository.findById(id);
            if (produtoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found"));
            }
            Produto produto = produtoOpt.get();
            if (request.getNome() != null) produto.setNome(request.getNome());
            if (request.getDescricao() != null) produto.setDescricao(request.getDescricao());
            if (request.getPreco() != null) produto.setPreco(request.getPreco());
            if (request.getQuantidadeEstoque() != null) produto.setQuantidadeEstoque(request.getQuantidadeEstoque());
            if (request.getCategoriaId() != null) {
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(request.getCategoriaId());
                categoriaOpt.ifPresent(produto::setCategoria);
            }
            if (request.getUrlImagem() != null) produto.setUrlImagem(request.getUrlImagem());
            if (request.getAtivo() != null) produto.setAtivo(request.getAtivo());
            produto = produtoRepository.save(produto);
            return ResponseEntity.ok(toProdutoResponseDTO(produto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            if (!produtoRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found"));
            }
            produtoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete product: " + e.getMessage()));
        }
    }

    // ==================== SERVICES ====================

    @GetMapping("/api/services")
    public ResponseEntity<List<ServicoResponseDTO>> getAllServices() {
        List<Servico> servicos = servicoRepository.findAll();
        List<ServicoResponseDTO> response = servicos.stream()
                .map(this::toServicoResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/services/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable Long id) {
        Optional<Servico> servicoOpt = servicoRepository.findById(id);
        if (servicoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Service not found"));
        }
        return ResponseEntity.ok(toServicoResponseDTO(servicoOpt.get()));
    }

    @PostMapping("/api/services")
    public ResponseEntity<?> createService(@RequestBody ServicoRequestDTO request) {
        try {
            Servico servico = new Servico();
            servico.setNome(request.getNome());
            servico.setDescricao(request.getDescricao());
            servico.setPreco(request.getPreco());
            servico.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);
            servico = servicoRepository.save(servico);
            return ResponseEntity.status(HttpStatus.CREATED).body(toServicoResponseDTO(servico));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create service: " + e.getMessage()));
        }
    }

    @PutMapping("/api/services/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody ServicoRequestDTO request) {
        try {
            Optional<Servico> servicoOpt = servicoRepository.findById(id);
            if (servicoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Service not found"));
            }
            Servico servico = servicoOpt.get();
            if (request.getNome() != null) servico.setNome(request.getNome());
            if (request.getDescricao() != null) servico.setDescricao(request.getDescricao());
            if (request.getPreco() != null) servico.setPreco(request.getPreco());
            if (request.getAtivo() != null) servico.setAtivo(request.getAtivo());
            servico = servicoRepository.save(servico);
            return ResponseEntity.ok(toServicoResponseDTO(servico));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update service: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            if (!servicoRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Service not found"));
            }
            servicoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Service deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete service: " + e.getMessage()));
        }
    }

    // ==================== DTOs ====================

    private CategoriaResponseDTO toCategoriaResponseDTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setDescricao(categoria.getDescricao());
        return dto;
    }

    private ProdutoResponseDTO toProdutoResponseDTO(Produto produto) {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        if (produto.getCategoria() != null) {
            dto.setCategoriaId(produto.getCategoria().getId());
            dto.setCategoriaNome(produto.getCategoria().getNome());
        }
        dto.setUrlImagem(produto.getUrlImagem());
        dto.setAtivo(produto.getAtivo());
        return dto;
    }

    private ServicoResponseDTO toServicoResponseDTO(Servico servico) {
        ServicoResponseDTO dto = new ServicoResponseDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setAtivo(servico.getAtivo());
        return dto;
    }
}
