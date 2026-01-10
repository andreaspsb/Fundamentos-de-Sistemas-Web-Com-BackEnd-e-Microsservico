package com.petshop.service;

import com.petshop.model.Pedido;
import com.petshop.model.Pedido.StatusPedido;
import com.petshop.model.ItemPedido;
import com.petshop.model.Cliente;
import com.petshop.model.Produto;
import com.petshop.repository.PedidoRepository;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.ProdutoRepository;
import com.petshop.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(@NonNull Long id) {
        return pedidoRepository.findById(ValidationUtils.requireNonNullId(id, "Pedido"));
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorCliente(@NonNull Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataPedidoDesc(ValidationUtils.requireNonNullId(clienteId, "Cliente"));
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return pedidoRepository.findByDataPedidoBetween(dataInicio, dataFim);
    }

    @Transactional
    public Pedido criar(@NonNull Long clienteId) {
        Long safeClienteId = ValidationUtils.requireNonNullId(clienteId, "Cliente");
        Cliente cliente = clienteRepository.findById(safeClienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + clienteId));
        
        Pedido pedido = new Pedido(cliente);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido adicionarItem(@NonNull Long pedidoId, @NonNull Long produtoId, @NonNull Integer quantidade) {
        Long safePedidoId = ValidationUtils.requireNonNullId(pedidoId, "Pedido");
        Long safeProdutoId = ValidationUtils.requireNonNullId(produtoId, "Produto");
        Pedido pedido = pedidoRepository.findById(safePedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new RuntimeException("Não é possível adicionar itens a um pedido que não está pendente");
        }

        Produto produto = produtoRepository.findById(safeProdutoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + produtoId));

        if (!produto.getAtivo()) {
            throw new RuntimeException("Produto não está ativo");
        }

        if (!produto.temEstoque(quantidade)) {
            throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        ItemPedido item = new ItemPedido(produto, quantidade);
        pedido.adicionarItem(item);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido removerItem(@NonNull Long pedidoId, @NonNull Long itemId) {
        Long safePedidoId = ValidationUtils.requireNonNullId(pedidoId, "Pedido");
        ValidationUtils.requireNonNullId(itemId, "Item");
        Pedido pedido = pedidoRepository.findById(safePedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new RuntimeException("Não é possível remover itens de um pedido que não está pendente");
        }

        ItemPedido item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado no pedido"));

        pedido.removerItem(item);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido confirmar(@NonNull Long id) {
        Pedido pedido = pedidoRepository.findById(ValidationUtils.requireNonNullId(id, "Pedido"))
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));

        if (pedido.getItens().isEmpty()) {
            throw new RuntimeException("Não é possível confirmar um pedido sem itens");
        }

        // Reduzir estoque dos produtos
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (!produto.temEstoque(item.getQuantidade())) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }
            produto.reduzirEstoque(item.getQuantidade());
            produtoRepository.save(produto);
        }

        pedido.setStatus(StatusPedido.CONFIRMADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarStatus(@NonNull Long id, @NonNull StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(ValidationUtils.requireNonNullId(id, "Pedido"))
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelar(@NonNull Long id) {
        Pedido pedido = pedidoRepository.findById(ValidationUtils.requireNonNullId(id, "Pedido"))
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));

        if (pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw new RuntimeException("Não é possível cancelar um pedido já entregue");
        }

        // Se o pedido foi confirmado, devolver estoque
        if (pedido.getStatus() == StatusPedido.CONFIRMADO || 
            pedido.getStatus() == StatusPedido.PROCESSANDO ||
            pedido.getStatus() == StatusPedido.ENVIADO) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.adicionarEstoque(item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void deletar(Long id) {
        Long safeId = ValidationUtils.requireNonNullId(id, "Pedido");
        Pedido pedido = pedidoRepository.findById(safeId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));

        if (pedido.getStatus() != StatusPedido.PENDENTE && pedido.getStatus() != StatusPedido.CANCELADO) {
            throw new RuntimeException("Apenas pedidos pendentes ou cancelados podem ser deletados");
        }

        pedidoRepository.deleteById(safeId);
    }

    @Transactional(readOnly = true)
    public Long contarPorStatus(StatusPedido status) {
        return pedidoRepository.countByStatus(status);
    }
}
