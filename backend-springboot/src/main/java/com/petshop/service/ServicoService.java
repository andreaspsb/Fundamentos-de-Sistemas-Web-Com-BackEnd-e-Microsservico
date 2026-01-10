package com.petshop.service;

import com.petshop.model.Servico;
import com.petshop.repository.ServicoRepository;
import com.petshop.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Servico> listarAtivos() {
        return servicoRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public Optional<Servico> buscarPorId(@NonNull Long id) {
        return servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"));
    }

    @Transactional(readOnly = true)
    public Optional<Servico> buscarPorNome(String nome) {
        return servicoRepository.findByNome(nome);
    }

    @Transactional
    public Servico salvar(@NonNull Servico servico) {
        ValidationUtils.requireNonNullEntity(servico, "Serviço");
        if (servicoRepository.existsByNome(servico.getNome())) {
            throw new RuntimeException("Já existe um serviço com este nome");
        }
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(@NonNull Long id, @NonNull Servico servicoAtualizado) {
        ValidationUtils.requireNonNullId(id, "Serviço");
        ValidationUtils.requireNonNullEntity(servicoAtualizado, "Serviço");
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com ID: " + id));

        // Validar nome único se foi alterado
        if (!servico.getNome().equals(servicoAtualizado.getNome()) && 
            servicoRepository.existsByNome(servicoAtualizado.getNome())) {
            throw new RuntimeException("Já existe um serviço com este nome");
        }

        servico.setNome(servicoAtualizado.getNome());
        servico.setDescricao(servicoAtualizado.getDescricao());
        servico.setPreco(servicoAtualizado.getPreco());
        servico.setAtivo(servicoAtualizado.getAtivo());

        return servicoRepository.save(servico);
    }

    @Transactional
    public void ativar(@NonNull Long id) {
        Servico servico = servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"))
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com ID: " + id));
        servico.setAtivo(true);
        servicoRepository.save(servico);
    }

    @Transactional
    public void desativar(@NonNull Long id) {
        Servico servico = servicoRepository.findById(ValidationUtils.requireNonNullId(id, "Serviço"))
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com ID: " + id));
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    @Transactional
    public void deletar(@NonNull Long id) {
        Long safeId = ValidationUtils.requireNonNullId(id, "Serviço");
        if (!servicoRepository.existsById(safeId)) {
            throw new RuntimeException("Serviço não encontrado com ID: " + id);
        }
        servicoRepository.deleteById(safeId);
    }
}
