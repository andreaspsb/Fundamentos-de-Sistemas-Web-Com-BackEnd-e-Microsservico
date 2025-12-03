package com.petshop.controller;

import com.petshop.dto.ItemPedidoDTO;
import com.petshop.dto.ItemPedidoRequestDTO;
import com.petshop.dto.PedidoRequestDTO;
import com.petshop.dto.PedidoResponseDTO;
import com.petshop.model.Pedido;
import com.petshop.model.Pedido.StatusPedido;
import com.petshop.service.PedidoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos (Carrinho)", description = "Gerenciamento de pedidos e carrinho de compras")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        List<PedidoResponseDTO> pedidos = pedidoService.listarTodos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(pedido -> ResponseEntity.ok(toResponseDTO(pedido)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorCliente(@PathVariable Long clienteId) {
        List<PedidoResponseDTO> pedidos = pedidoService.buscarPorCliente(clienteId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorStatus(@PathVariable String status) {
        StatusPedido statusEnum = StatusPedido.valueOf(status.toUpperCase());
        List<PedidoResponseDTO> pedidos = pedidoService.buscarPorStatus(statusEnum)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> contarPorStatus(@PathVariable String status) {
        StatusPedido statusEnum = StatusPedido.valueOf(status.toUpperCase());
        Long count = pedidoService.contarPorStatus(statusEnum);
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO dto) {
        Pedido pedido = pedidoService.criar(dto.getClienteId());
        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setObservacoes(dto.getObservacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(pedido));
    }

    @PostMapping("/{pedidoId}/itens")
    public ResponseEntity<PedidoResponseDTO> adicionarItem(
            @PathVariable Long pedidoId,
            @Valid @RequestBody ItemPedidoRequestDTO dto) {
        Pedido pedido = pedidoService.adicionarItem(pedidoId, dto.getProdutoId(), dto.getQuantidade());
        return ResponseEntity.ok(toResponseDTO(pedido));
    }

    @DeleteMapping("/{pedidoId}/itens/{itemId}")
    public ResponseEntity<PedidoResponseDTO> removerItem(
            @PathVariable Long pedidoId,
            @PathVariable Long itemId) {
        Pedido pedido = pedidoService.removerItem(pedidoId, itemId);
        return ResponseEntity.ok(toResponseDTO(pedido));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmar(@PathVariable Long id) {
        Pedido pedido = pedidoService.confirmar(id);
        return ResponseEntity.ok(toResponseDTO(pedido));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        StatusPedido statusEnum = StatusPedido.valueOf(status.toUpperCase());
        Pedido pedido = pedidoService.atualizarStatus(id, statusEnum);
        return ResponseEntity.ok(toResponseDTO(pedido));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id) {
        Pedido pedido = pedidoService.cancelar(id);
        return ResponseEntity.ok(toResponseDTO(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pedidoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Métodos de conversão
    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setDataPedido(pedido.getDataPedido());
        dto.setValorTotal(pedido.getValorTotal());
        dto.setStatus(pedido.getStatus().toString());
        dto.setFormaPagamento(pedido.getFormaPagamento());
        dto.setObservacoes(pedido.getObservacoes());
        dto.setClienteId(pedido.getCliente().getId());
        dto.setClienteNome(pedido.getCliente().getNome());
        dto.setClienteTelefone(pedido.getCliente().getTelefone());
        dto.setItens(pedido.getItens().stream()
                .map(item -> new ItemPedidoDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList()));
        return dto;
    }
}
