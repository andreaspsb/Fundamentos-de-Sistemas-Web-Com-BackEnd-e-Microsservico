package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Pedido;
import com.petshop.functions.shared.model.Pedido.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByClienteIdAndStatus(Long clienteId, StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.dataPedido BETWEEN :dataInicio AND :dataFim")
    List<Pedido> findByDataPedidoBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.dataPedido DESC")
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(@Param("clienteId") Long clienteId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status = :status")
    Long countByStatus(@Param("status") StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.status = :status AND p.dataPedido < :limite")
    List<Pedido> findByStatusAndDataPedidoBefore(@Param("status") StatusPedido status, @Param("limite") LocalDateTime limite);
}
