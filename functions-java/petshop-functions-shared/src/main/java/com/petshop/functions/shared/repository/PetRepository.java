package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByClienteId(Long clienteId);

    List<Pet> findByTipo(String tipo);

    List<Pet> findByClienteIdAndTipo(Long clienteId, String tipo);
}
