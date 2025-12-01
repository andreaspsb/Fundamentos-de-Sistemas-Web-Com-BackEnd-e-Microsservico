package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Agendamento;
import com.petshop.functions.shared.model.Agendamento.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByClienteId(Long clienteId);

    List<Agendamento> findByPetId(Long petId);

    List<Agendamento> findByStatus(StatusAgendamento status);

    List<Agendamento> findByDataAgendamento(LocalDate dataAgendamento);

    List<Agendamento> findByDataAgendamentoAndStatus(LocalDate dataAgendamento, StatusAgendamento status);

    @Query("SELECT a FROM Agendamento a WHERE a.dataAgendamento = :data AND a.horario = :horario")
    List<Agendamento> findByDataAndHorario(@Param("data") LocalDate data, @Param("horario") LocalTime horario);

    @Query("SELECT a FROM Agendamento a WHERE a.dataAgendamento BETWEEN :dataInicio AND :dataFim")
    List<Agendamento> findByDataAgendamentoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    boolean existsByDataAgendamentoAndHorario(LocalDate dataAgendamento, LocalTime horario);
}
