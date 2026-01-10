package com.petshop.controller;

import com.petshop.dto.ProdutoRequestDTO;
import com.petshop.dto.ProdutoResponseDTO;
import com.petshop.model.Produto;
import com.petshop.service.ProdutoService;
import com.petshop.util.ValidationUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Catálogo de produtos do Pet Shop")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {
        List<ProdutoResponseDTO> produtos = produtoService.listarTodos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<ProdutoResponseDTO>> listarDisponiveis() {
        List<ProdutoResponseDTO> produtos = produtoService.listarDisponiveis()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(ValidationUtils.requireNonNullId(id, "Produto"))
                .map(produto -> ResponseEntity.ok(toResponseDTO(produto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProdutoResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        List<ProdutoResponseDTO> produtos = produtoService.listarPorCategoria(ValidationUtils.requireNonNullId(categoriaId, "Categoria"))
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/categoria/{categoriaId}/disponiveis")
    public ResponseEntity<List<ProdutoResponseDTO>> listarDisponiveisPorCategoria(@PathVariable Long categoriaId) {
        List<ProdutoResponseDTO> produtos = produtoService.listarDisponiveisPorCategoria(ValidationUtils.requireNonNullId(categoriaId, "Categoria"))
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorNome(@RequestParam String termo) {
        List<ProdutoResponseDTO> produtos = produtoService.buscarPorNome(termo)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ProdutoResponseDTO>> listarEstoqueBaixo(@RequestParam(defaultValue = "10") Integer quantidade) {
        List<ProdutoResponseDTO> produtos = produtoService.listarEstoqueBaixo(quantidade)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        Produto produto = ValidationUtils.requireNonNullEntity(toEntity(dto), "Produto");
        Produto produtoSalvo = produtoService.salvar(produto, ValidationUtils.requireNonNullId(dto.getCategoriaId(), "Categoria"));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(produtoSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequestDTO dto) {
        Produto produto = ValidationUtils.requireNonNullEntity(toEntity(dto), "Produto");
        Produto produtoAtualizado = produtoService.atualizar(ValidationUtils.requireNonNullId(id, "Produto"), produto);
        return ResponseEntity.ok(toResponseDTO(produtoAtualizado));
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<ProdutoResponseDTO> atualizarEstoque(
            @PathVariable Long id,
            @RequestParam Integer quantidade) {
        Produto produto = produtoService.atualizarEstoque(ValidationUtils.requireNonNullId(id, "Produto"), ValidationUtils.requireNonNullQuantity(quantidade, "Quantidade"));
        return ResponseEntity.ok(toResponseDTO(produto));
    }

    @PatchMapping("/{id}/adicionar-estoque")
    public ResponseEntity<ProdutoResponseDTO> adicionarEstoque(
            @PathVariable Long id,
            @RequestParam Integer quantidade) {
        Produto produto = produtoService.adicionarEstoque(ValidationUtils.requireNonNullId(id, "Produto"), ValidationUtils.requireNonNullQuantity(quantidade, "Quantidade"));
        return ResponseEntity.ok(toResponseDTO(produto));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        produtoService.ativar(ValidationUtils.requireNonNullId(id, "Produto"));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        produtoService.desativar(ValidationUtils.requireNonNullId(id, "Produto"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(ValidationUtils.requireNonNullId(id, "Produto"));
        return ResponseEntity.noContent().build();
    }

    // Métodos de conversão
    private ProdutoResponseDTO toResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getUrlImagem(),
                produto.getAtivo(),
                produto.getCategoria().getId(),
                produto.getCategoria().getNome()
        );
    }

    private Produto toEntity(ProdutoRequestDTO dto) {
        return new Produto(
                dto.getNome(),
                dto.getDescricao(),
                dto.getPreco(),
                dto.getQuantidadeEstoque(),
                dto.getUrlImagem()
        );
    }
}
