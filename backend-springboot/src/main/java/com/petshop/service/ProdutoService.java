package com.petshop.service;

import com.petshop.model.Produto;
import com.petshop.model.Categoria;
import com.petshop.repository.ProdutoRepository;
import com.petshop.repository.CategoriaRepository;
import com.petshop.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Produto> listarDisponiveis() {
        return produtoRepository.findProdutosDisponiveis();
    }

    @Transactional(readOnly = true)
    public List<Produto> listarPorCategoria(@NonNull Long categoriaId) {
        return produtoRepository.findByCategoriaId(ValidationUtils.requireNonNullId(categoriaId, "Categoria"));
    }

    @Transactional(readOnly = true)
    public List<Produto> listarDisponiveisPorCategoria(@NonNull Long categoriaId) {
        return produtoRepository.findProdutosDisponiveisPorCategoria(ValidationUtils.requireNonNullId(categoriaId, "Categoria"));
    }

    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(@NonNull Long id) {
        return produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"));
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(String termo) {
        return produtoRepository.buscarPorNome(termo);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarEstoqueBaixo(Integer quantidade) {
        return produtoRepository.findByQuantidadeEstoqueLessThan(quantidade);
    }

    @Transactional
    public Produto salvar(@NonNull Produto produto, @NonNull Long categoriaId) {
        ValidationUtils.requireNonNullEntity(produto, "Produto");
        Long safeCategoriaId = ValidationUtils.requireNonNullId(categoriaId, "Categoria");
        Categoria categoria = categoriaRepository.findById(safeCategoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));
        
        produto.setCategoria(categoria);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(@NonNull Long id, @NonNull Produto produtoAtualizado) {
        ValidationUtils.requireNonNullId(id, "Produto");
        ValidationUtils.requireNonNullEntity(produtoAtualizado, "Produto");
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        produto.setNome(produtoAtualizado.getNome());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());
        produto.setUrlImagem(produtoAtualizado.getUrlImagem());
        produto.setAtivo(produtoAtualizado.getAtivo());

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarEstoque(@NonNull Long id, @NonNull Integer quantidade) {
        Produto produto = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"))
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        
        produto.setQuantidadeEstoque(quantidade);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto adicionarEstoque(@NonNull Long id, @NonNull Integer quantidade) {
        Produto produto = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"))
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        
        produto.adicionarEstoque(quantidade);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto reduzirEstoque(@NonNull Long id, @NonNull Integer quantidade) {
        Produto produto = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"))
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        
        if (!produto.temEstoque(quantidade)) {
            throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
        }
        
        produto.reduzirEstoque(quantidade);
        return produtoRepository.save(produto);
    }

    @Transactional
    public void ativar(@NonNull Long id) {
        Produto produto = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"))
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        produto.setAtivo(true);
        produtoRepository.save(produto);
    }

    @Transactional
    public void desativar(@NonNull Long id) {
        Produto produto = produtoRepository.findById(ValidationUtils.requireNonNullId(id, "Produto"))
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional
    public void deletar(@NonNull Long id) {
        Long safeId = ValidationUtils.requireNonNullId(id, "Produto");
        if (!produtoRepository.existsById(safeId)) {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
        produtoRepository.deleteById(safeId);
    }
}
