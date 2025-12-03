package com.petshop.service;

import com.petshop.model.Pedido;
import com.petshop.model.Pedido.StatusPedido;
import com.petshop.model.ItemPedido;
import com.petshop.model.Cliente;
import com.petshop.model.Produto;
import com.petshop.repository.PedidoRepository;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
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
    public Pedido criar(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + clienteId));
        
        Pedido pedido = new Pedido(cliente);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido adicionarItem(Long pedidoId, Long produtoId, Integer quantidade) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + pedidoId));

        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new RuntimeException("Não é possível adicionar itens a um pedido que não está pendente");
        }

        Produto produto = produtoRepository.findById(produtoId)
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
    public Pedido removerItem(Long pedidoId, Long itemId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
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
    public Pedido confirmar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
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
    public Pedido atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
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
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));

        if (pedido.getStatus() != StatusPedido.PENDENTE && pedido.getStatus() != StatusPedido.CANCELADO) {
            throw new RuntimeException("Apenas pedidos pendentes ou cancelados podem ser deletados");
        }

        pedidoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long contarPorStatus(StatusPedido status) {
        return pedidoRepository.countByStatus(status);
    }
}
