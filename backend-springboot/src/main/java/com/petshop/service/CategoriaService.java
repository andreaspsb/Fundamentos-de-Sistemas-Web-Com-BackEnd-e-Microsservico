package com.petshop.service;

import com.petshop.model.Categoria;
import com.petshop.repository.CategoriaRepository;
import com.petshop.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarAtivas() {
        return categoriaRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(@NonNull Long id) {
        return categoriaRepository.findById(ValidationUtils.requireNonNullId(id, "Categoria"));
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorNome(String nome) {
        return categoriaRepository.findByNome(nome);
    }

    @Transactional
    public Categoria salvar(@NonNull Categoria categoria) {
        ValidationUtils.requireNonNullEntity(categoria, "Categoria");
        if (categoriaRepository.existsByNome(categoria.getNome())) {
            throw new RuntimeException("Já existe uma categoria com este nome");
        }
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(@NonNull Long id, @NonNull Categoria categoriaAtualizada) {
        ValidationUtils.requireNonNullId(id, "Categoria");
        ValidationUtils.requireNonNullEntity(categoriaAtualizada, "Categoria");
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));

        // Validar nome único se foi alterado
        if (!categoria.getNome().equals(categoriaAtualizada.getNome()) && 
            categoriaRepository.existsByNome(categoriaAtualizada.getNome())) {
            throw new RuntimeException("Já existe uma categoria com este nome");
        }

        categoria.setNome(categoriaAtualizada.getNome());
        categoria.setDescricao(categoriaAtualizada.getDescricao());
        categoria.setAtivo(categoriaAtualizada.getAtivo());

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void ativar(@NonNull Long id) {
        Categoria categoria = categoriaRepository.findById(ValidationUtils.requireNonNullId(id, "Categoria"))
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        categoria.setAtivo(true);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void desativar(@NonNull Long id) {
        Categoria categoria = categoriaRepository.findById(ValidationUtils.requireNonNullId(id, "Categoria"))
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void deletar(@NonNull Long id) {
        Long safeId = ValidationUtils.requireNonNullId(id, "Categoria");
        if (!categoriaRepository.existsById(safeId)) {
            throw new RuntimeException("Categoria não encontrada com ID: " + id);
        }
        categoriaRepository.deleteById(safeId);
    }
}
