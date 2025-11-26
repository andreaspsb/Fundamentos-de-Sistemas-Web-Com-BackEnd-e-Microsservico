package com.petshop.service;

import com.petshop.model.Cliente;
import com.petshop.model.ItemPedido;
import com.petshop.model.Pedido;
import com.petshop.model.Pedido.StatusPedido;
import com.petshop.model.Produto;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.PedidoRepository;
import com.petshop.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private Cliente cliente;
    private Produto produto;
    private ItemPedido itemPedido;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("12345678901");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Ração Premium");
        produto.setPreco(89.90);
        produto.setQuantidadeEstoque(50);
        produto.setAtivo(true);

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente(cliente);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setValorTotal(0.0);
        pedido.setItens(new ArrayList<>());

        itemPedido = new ItemPedido();
        itemPedido.setId(1L);
        itemPedido.setProduto(produto);
        itemPedido.setQuantidade(2);
        itemPedido.setPrecoUnitario(89.90);
        itemPedido.setSubtotal(179.80);
    }

    // ========== ListarTodos Tests ==========

    @Test
    void testListarTodos() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository, times(1)).findAll();
    }

    // ========== BuscarPorId Tests ==========

    @Test
    void testBuscarPorId() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        Optional<Pedido> resultado = pedidoService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Pedido> resultado = pedidoService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(pedidoRepository, times(1)).findById(999L);
    }

    // ========== BuscarPorCliente Tests ==========

    @Test
    void testBuscarPorCliente() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByClienteIdOrderByDataPedidoDesc(1L)).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.buscarPorCliente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository, times(1)).findByClienteIdOrderByDataPedidoDesc(1L);
    }

    // ========== BuscarPorStatus Tests ==========

    @Test
    void testBuscarPorStatus() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByStatus(StatusPedido.PENDENTE)).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.buscarPorStatus(StatusPedido.PENDENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(StatusPedido.PENDENTE, resultado.get(0).getStatus());
        verify(pedidoRepository, times(1)).findByStatus(StatusPedido.PENDENTE);
    }

    // ========== BuscarPorPeriodo Tests ==========

    @Test
    void testBuscarPorPeriodo() {
        // Arrange
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(7);
        LocalDateTime dataFim = LocalDateTime.now();
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByDataPedidoBetween(dataInicio, dataFim)).thenReturn(pedidos);

        // Act
        List<Pedido> resultado = pedidoService.buscarPorPeriodo(dataInicio, dataFim);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pedidoRepository, times(1)).findByDataPedidoBetween(dataInicio, dataFim);
    }

    // ========== Criar Tests ==========

    @Test
    void testCriar() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.criar(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        verify(clienteRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testCriarClienteNaoEncontrado() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.criar(999L);
        });

        assertEquals("Cliente não encontrado com ID: 999", exception.getMessage());
        verify(clienteRepository, times(1)).findById(999L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // ========== AdicionarItem Tests ==========

    @Test
    void testAdicionarItem() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.adicionarItem(1L, 1L, 2);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getItens().size());
        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testAdicionarItemPedidoNaoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.adicionarItem(999L, 1L, 2);
        });

        assertEquals("Pedido não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testAdicionarItemPedidoNaoPendente() {
        // Arrange
        pedido.setStatus(StatusPedido.CONFIRMADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.adicionarItem(1L, 1L, 2);
        });

        assertTrue(exception.getMessage().contains("não está pendente"));
    }

    @Test
    void testAdicionarItemProdutoNaoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.adicionarItem(1L, 999L, 2);
        });

        assertEquals("Produto não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testAdicionarItemProdutoInativo() {
        // Arrange
        produto.setAtivo(false);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.adicionarItem(1L, 1L, 2);
        });

        assertEquals("Produto não está ativo", exception.getMessage());
    }

    @Test
    void testAdicionarItemEstoqueInsuficiente() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.adicionarItem(1L, 1L, 100); // Mais que o estoque
        });

        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
    }

    // ========== RemoverItem Tests ==========

    @Test
    void testRemoverItem() {
        // Arrange
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.removerItem(1L, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.getItens().size());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testRemoverItemPedidoNaoPendente() {
        // Arrange
        pedido.setStatus(StatusPedido.CONFIRMADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.removerItem(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("não está pendente"));
    }

    @Test
    void testRemoverItemNaoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.removerItem(1L, 999L);
        });

        assertEquals("Item não encontrado no pedido", exception.getMessage());
    }

    // ========== Confirmar Tests ==========

    @Test
    void testConfirmar() {
        // Arrange
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.confirmar(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusPedido.CONFIRMADO, resultado.getStatus());
        verify(produtoRepository, times(1)).save(any(Produto.class));
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testConfirmarPedidoSemItens() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.confirmar(1L);
        });

        assertTrue(exception.getMessage().contains("sem itens"));
    }

    @Test
    void testConfirmarEstoqueInsuficiente() {
        // Arrange
        itemPedido.setQuantidade(100); // Mais que o estoque
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.confirmar(1L);
        });

        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
    }

    // ========== AtualizarStatus Tests ==========

    @Test
    void testAtualizarStatus() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.atualizarStatus(1L, StatusPedido.ENVIADO);

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusPedido.ENVIADO, resultado.getStatus());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testAtualizarStatusPedidoNaoEncontrado() {
        // Arrange
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.atualizarStatus(999L, StatusPedido.ENVIADO);
        });

        assertEquals("Pedido não encontrado com ID: 999", exception.getMessage());
    }

    // ========== Cancelar Tests ==========

    @Test
    void testCancelarPedidoPendente() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        pedidoService.cancelar(1L);

        // Assert
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testCancelarPedidoConfirmadoDevolveEstoque() {
        // Arrange
        pedido.setStatus(StatusPedido.CONFIRMADO);
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido);
        produto.setQuantidadeEstoque(48); // Já reduzido
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        pedidoService.cancelar(1L);

        // Assert
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(50, produto.getQuantidadeEstoque()); // 48 + 2
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testCancelarPedidoEntregue() {
        // Arrange
        pedido.setStatus(StatusPedido.ENTREGUE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.cancelar(1L);
        });

        assertTrue(exception.getMessage().contains("já entregue"));
    }

    // ========== Deletar Tests ==========

    @Test
    void testDeletarPedidoPendente() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoRepository).deleteById(1L);

        // Act
        pedidoService.deletar(1L);

        // Assert
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarPedidoCancelado() {
        // Arrange
        pedido.setStatus(StatusPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoRepository).deleteById(1L);

        // Act
        pedidoService.deletar(1L);

        // Assert
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarPedidoConfirmado() {
        // Arrange
        pedido.setStatus(StatusPedido.CONFIRMADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.deletar(1L);
        });

        assertTrue(exception.getMessage().contains("pendentes ou cancelados"));
    }

    // ========== ContarPorStatus Tests ==========

    @Test
    void testContarPorStatus() {
        // Arrange
        when(pedidoRepository.countByStatus(StatusPedido.PENDENTE)).thenReturn(5L);

        // Act
        Long resultado = pedidoService.contarPorStatus(StatusPedido.PENDENTE);

        // Assert
        assertEquals(5L, resultado);
        verify(pedidoRepository, times(1)).countByStatus(StatusPedido.PENDENTE);
    }
}
