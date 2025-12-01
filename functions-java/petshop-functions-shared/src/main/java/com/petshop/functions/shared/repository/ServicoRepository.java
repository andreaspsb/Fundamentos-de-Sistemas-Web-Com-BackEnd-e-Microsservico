package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Optional<Servico> findByNome(String nome);

    List<Servico> findByAtivo(Boolean ativo);

    boolean existsByNome(String nome);
}
