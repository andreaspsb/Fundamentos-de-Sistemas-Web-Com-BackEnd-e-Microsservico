package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    Optional<Cliente> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
