package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    List<ItemPedido> findByPedidoId(Long pedidoId);

    List<ItemPedido> findByProdutoId(Long produtoId);

    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.cliente.id = :clienteId")
    List<ItemPedido> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT SUM(i.quantidade) FROM ItemPedido i WHERE i.produto.id = :produtoId")
    Long getTotalQuantidadeVendidaPorProduto(@Param("produtoId") Long produtoId);
}
