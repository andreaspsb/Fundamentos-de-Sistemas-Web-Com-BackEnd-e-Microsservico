package com.petshop.functions.orders;

import com.petshop.functions.shared.dto.ItemPedidoDTO;
import com.petshop.functions.shared.dto.ItemPedidoRequestDTO;
import com.petshop.functions.shared.dto.PedidoRequestDTO;
import com.petshop.functions.shared.dto.PedidoResponseDTO;
import com.petshop.functions.shared.model.Cliente;
import com.petshop.functions.shared.model.ItemPedido;
import com.petshop.functions.shared.model.Pedido;
import com.petshop.functions.shared.model.Pedido.StatusPedido;
import com.petshop.functions.shared.model.Produto;
import com.petshop.functions.shared.repository.ClienteRepository;
import com.petshop.functions.shared.repository.ItemPedidoRepository;
import com.petshop.functions.shared.repository.PedidoRepository;
import com.petshop.functions.shared.repository.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OrderController {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public OrderController(PedidoRepository pedidoRepository,
                           ItemPedidoRepository itemPedidoRepository,
                           ClienteRepository clienteRepository,
                           ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    // === HEALTH CHECK ===
    
    @GetMapping("/orders/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "orders");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // === PEDIDOS ===
    
    @GetMapping("/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String status) {
        
        List<Pedido> pedidos;
        
        if (clienteId != null) {
            pedidos = pedidoRepository.findByClienteId(clienteId);
        } else if (status != null) {
            try {
                StatusPedido statusEnum = StatusPedido.valueOf(status.toUpperCase());
                pedidos = pedidoRepository.findByStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                pedidos = pedidoRepository.findAll();
            }
        } else {
            pedidos = pedidoRepository.findAll();
        }
        
        List<PedidoResponseDTO> response = pedidos.stream()
            .map(this::toPedidoResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<?> getPedidoById(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoRepository.findById(id);
        
        if (pedido.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Pedido não encontrado"));
        }
        
        return ResponseEntity.ok(toPedidoResponse(pedido.get()));
    }

    @PostMapping("/pedidos")
    public ResponseEntity<?> createPedido(@Valid @RequestBody PedidoRequestDTO request) {
        // Validar cliente
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.getClienteId());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Cliente não encontrado"));
        }
        
        // Validar itens
        if (request.getItens() == null || request.getItens().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "O pedido deve conter pelo menos um item"));
        }
        
        // Criar pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteOpt.get());
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setFormaPagamento(request.getFormaPagamento());
        pedido.setObservacoes(request.getObservacoes());
        pedido.setStatus(StatusPedido.PENDENTE);
        
        // Processar itens
        List<ItemPedido> itens = new ArrayList<>();
        double valorTotal = 0.0;
        
        for (ItemPedidoRequestDTO itemDto : request.getItens()) {
            Optional<Produto> produtoOpt = produtoRepository.findById(itemDto.getProdutoId());
            if (produtoOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Produto não encontrado: " + itemDto.getProdutoId()));
            }
            
            Produto produto = produtoOpt.get();
            
            // Verificar estoque
            if (produto.getQuantidadeEstoque() < itemDto.getQuantidade()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estoque insuficiente para o produto: " + produto.getNome()));
            }
            
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco() * itemDto.getQuantidade());
            
            itens.add(item);
            valorTotal += item.getSubtotal();
            
            // Atualizar estoque
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemDto.getQuantidade());
            produtoRepository.save(produto);
        }
        
        pedido.setValorTotal(valorTotal);
        pedido.setItens(itens);
        
        Pedido saved = pedidoRepository.save(pedido);
        
        // Salvar itens
        for (ItemPedido item : itens) {
            item.setPedido(saved);
            itemPedidoRepository.save(item);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toPedidoResponse(saved));
    }

    @PatchMapping("/pedidos/{id}/status")
    public ResponseEntity<?> updatePedidoStatus(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        Optional<Pedido> existingOpt = pedidoRepository.findById(id);
        
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Pedido não encontrado"));
        }
        
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Status é obrigatório"));
        }
        
        StatusPedido newStatus;
        try {
            newStatus = StatusPedido.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Status inválido"));
        }
        
        Pedido pedido = existingOpt.get();
        
        // Se cancelando, devolver estoque
        if (newStatus == StatusPedido.CANCELADO && pedido.getStatus() != StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }
        
        pedido.setStatus(newStatus);
        Pedido updated = pedidoRepository.save(pedido);
        
        return ResponseEntity.ok(toPedidoResponse(updated));
    }

    @DeleteMapping("/pedidos/{id}")
    public ResponseEntity<?> deletePedido(@PathVariable Long id) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        
        if (pedidoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Pedido não encontrado"));
        }
        
        Pedido pedido = pedidoOpt.get();
        
        // Devolver estoque se não cancelado
        if (pedido.getStatus() != StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }
        
        pedidoRepository.delete(pedido);
        
        return ResponseEntity.ok(Map.of("message", "Pedido excluído com sucesso"));
    }

    // === MÉTODOS AUXILIARES ===
    
    private PedidoResponseDTO toPedidoResponse(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setDataPedido(pedido.getDataPedido());
        dto.setValorTotal(pedido.getValorTotal());
        dto.setStatus(pedido.getStatus().name());
        dto.setFormaPagamento(pedido.getFormaPagamento());
        dto.setObservacoes(pedido.getObservacoes());
        
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
            dto.setClienteTelefone(pedido.getCliente().getTelefone());
        }
        
        if (pedido.getItens() != null) {
            dto.setItens(pedido.getItens().stream()
                .map(this::toItemPedidoDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private ItemPedidoDTO toItemPedidoDTO(ItemPedido item) {
        ItemPedidoDTO dto = new ItemPedidoDTO();
        dto.setId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        dto.setSubtotal(item.getSubtotal());
        
        if (item.getProduto() != null) {
            dto.setProdutoId(item.getProduto().getId());
            dto.setProdutoNome(item.getProduto().getNome());
        }
        
        return dto;
    }
}
