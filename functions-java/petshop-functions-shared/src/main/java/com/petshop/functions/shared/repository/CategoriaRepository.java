package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNome(String nome);

    List<Categoria> findByAtivo(Boolean ativo);

    boolean existsByNome(String nome);
}
