package com.petshop.controller;

import com.petshop.model.Cliente;
import com.petshop.model.ItemPedido;
import com.petshop.model.Pedido;
import com.petshop.model.Pedido.StatusPedido;
import com.petshop.model.Produto;
import com.petshop.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PedidoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
        cliente.setTelefone("11999999999");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Ração Premium");
        produto.setPreco(89.90);

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setValorTotal(89.90);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setFormaPagamento("pix");
        pedido.setObservacoes("Entregar pela manhã");
        pedido.setCliente(cliente);

        itemPedido = new ItemPedido();
        itemPedido.setId(1L);
        itemPedido.setProduto(produto);
        itemPedido.setQuantidade(1);
        itemPedido.setPrecoUnitario(89.90);
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido);
    }

    // ========== Testes de Listagem ==========

    @Test
    void testListarTodos() throws Exception {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.listarTodos()).thenReturn(pedidos);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].valorTotal").value(89.90))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].clienteNome").value("João Silva"));

        verify(pedidoService, times(1)).listarTodos();
    }

    @Test
    void testBuscarPorIdExistente() throws Exception {
        // Arrange
        when(pedidoService.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valorTotal").value(89.90))
                .andExpect(jsonPath("$.formaPagamento").value("pix"));

        verify(pedidoService, times(1)).buscarPorId(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(pedidoService.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/999"))
                .andExpect(status().isNotFound());

        verify(pedidoService, times(1)).buscarPorId(999L);
    }

    @Test
    void testBuscarPorCliente() throws Exception {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.buscarPorCliente(1L)).thenReturn(pedidos);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(1));

        verify(pedidoService, times(1)).buscarPorCliente(1L);
    }

    @Test
    void testBuscarPorStatus() throws Exception {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.buscarPorStatus(StatusPedido.PENDENTE)).thenReturn(pedidos);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/status/PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));

        verify(pedidoService, times(1)).buscarPorStatus(StatusPedido.PENDENTE);
    }

    @Test
    void testContarPorStatus() throws Exception {
        // Arrange
        when(pedidoService.contarPorStatus(StatusPedido.PENDENTE)).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/status/PENDENTE/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(pedidoService, times(1)).contarPorStatus(StatusPedido.PENDENTE);
    }

    // ========== Testes de Criação ==========

    @Test
    void testCriarPedido() throws Exception {
        // Arrange
        when(pedidoService.criar(1L)).thenReturn(pedido);

        String pedidoJson = """
            {
                "clienteId": 1,
                "formaPagamento": "pix",
                "observacoes": "Entregar pela manhã"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(1));

        verify(pedidoService, times(1)).criar(1L);
    }

    // ========== Testes de Itens ==========

    @Test
    void testAdicionarItem() throws Exception {
        // Arrange
        when(pedidoService.adicionarItem(1L, 1L, 2)).thenReturn(pedido);

        String itemJson = """
            {
                "produtoId": 1,
                "quantidade": 2
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/pedidos/1/itens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(itemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(pedidoService, times(1)).adicionarItem(1L, 1L, 2);
    }

    @Test
    void testRemoverItem() throws Exception {
        // Arrange
        pedido.getItens().clear();
        pedido.setValorTotal(0.0);
        when(pedidoService.removerItem(1L, 1L)).thenReturn(pedido);

        // Act & Assert
        mockMvc.perform(delete("/api/pedidos/1/itens/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(pedidoService, times(1)).removerItem(1L, 1L);
    }

    // ========== Testes de Status ==========

    @Test
    void testConfirmarPedido() throws Exception {
        // Arrange
        pedido.setStatus(StatusPedido.CONFIRMADO);
        when(pedidoService.confirmar(1L)).thenReturn(pedido);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos/1/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        verify(pedidoService, times(1)).confirmar(1L);
    }

    @Test
    void testAtualizarStatus() throws Exception {
        // Arrange
        pedido.setStatus(StatusPedido.ENVIADO);
        when(pedidoService.atualizarStatus(1L, StatusPedido.ENVIADO)).thenReturn(pedido);

        // Act & Assert
        mockMvc.perform(patch("/api/pedidos/1/status")
                .param("status", "ENVIADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));

        verify(pedidoService, times(1)).atualizarStatus(1L, StatusPedido.ENVIADO);
    }

    @Test
    void testCancelarPedido() throws Exception {
        // Arrange
        doNothing().when(pedidoService).cancelar(1L);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos/1/cancelar"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).cancelar(1L);
    }

    // ========== Testes de Deleção ==========

    @Test
    void testDeletarPedido() throws Exception {
        // Arrange
        doNothing().when(pedidoService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/pedidos/1"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).deletar(1L);
    }

    // ========== Testes de Itens no DTO ==========

    @Test
    void testListarComItens() throws Exception {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.listarTodos()).thenReturn(pedidos);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itens").isArray())
                .andExpect(jsonPath("$[0].itens[0].produtoId").value(1))
                .andExpect(jsonPath("$[0].itens[0].produtoNome").value("Ração Premium"))
                .andExpect(jsonPath("$[0].itens[0].quantidade").value(1))
                .andExpect(jsonPath("$[0].itens[0].precoUnitario").value(89.90));

        verify(pedidoService, times(1)).listarTodos();
    }
}
