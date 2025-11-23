package com.petshop.controller;

import com.petshop.model.Categoria;
import com.petshop.model.Produto;
import com.petshop.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProdutoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoService produtoService;

    private Produto produto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Ração");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Ração Premium");
        produto.setDescricao("Ração para cães adultos");
        produto.setPreco(89.90);
        produto.setQuantidadeEstoque(50);
        produto.setAtivo(true);
        produto.setCategoria(categoria);
    }

    @Test
    void testListarTodos() throws Exception {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoService.listarTodos()).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ração Premium"))
                .andExpect(jsonPath("$[0].preco").value(89.90))
                .andExpect(jsonPath("$[0].quantidadeEstoque").value(50));

        verify(produtoService, times(1)).listarTodos();
    }

    @Test
    void testListarDisponiveis() throws Exception {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoService.listarDisponiveis()).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/api/produtos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ração Premium"))
                .andExpect(jsonPath("$[0].ativo").value(true));

        verify(produtoService, times(1)).listarDisponiveis();
    }

    @Test
    void testBuscarPorIdExistente() throws Exception {
        // Arrange
        when(produtoService.buscarPorId(1L)).thenReturn(Optional.of(produto));

        // Act & Assert
        mockMvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ração Premium"))
                .andExpect(jsonPath("$.preco").value(89.90));

        verify(produtoService, times(1)).buscarPorId(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(produtoService.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/produtos/999"))
                .andExpect(status().isNotFound());

        verify(produtoService, times(1)).buscarPorId(999L);
    }

    @Test
    void testListarPorCategoria() throws Exception {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoService.listarPorCategoria(1L)).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/api/produtos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ração Premium"))
                .andExpect(jsonPath("$[0].categoriaId").value(1));

        verify(produtoService, times(1)).listarPorCategoria(1L);
    }

    @Test
    void testBuscarPorNome() throws Exception {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoService.buscarPorNome("Ração")).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/api/produtos/buscar")
                .param("termo", "Ração"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ração Premium"));

        verify(produtoService, times(1)).buscarPorNome("Ração");
    }

    @Test
    void testListarEstoqueBaixo() throws Exception {
        // Arrange
        produto.setQuantidadeEstoque(5);
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoService.listarEstoqueBaixo(10)).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/api/produtos/estoque-baixo")
                .param("quantidade", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidadeEstoque").value(5));

        verify(produtoService, times(1)).listarEstoqueBaixo(10);
    }

    @Test
    void testCriarProduto() throws Exception {
        // Arrange
        when(produtoService.salvar(any(Produto.class), eq(1L))).thenReturn(produto);

        String produtoJson = """
            {
                "nome": "Ração Premium",
                "descricao": "Ração para cães adultos",
                "preco": 89.90,
                "quantidadeEstoque": 50,
                "categoriaId": 1
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(produtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ração Premium"));

        verify(produtoService, times(1)).salvar(any(Produto.class), eq(1L));
    }

    @Test
    void testAtualizarProduto() throws Exception {
        // Arrange
        when(produtoService.atualizar(eq(1L), any(Produto.class))).thenReturn(produto);

        String produtoJson = """
            {
                "nome": "Ração Premium Plus",
                "descricao": "Nova descrição",
                "preco": 99.90,
                "quantidadeEstoque": 60,
                "ativo": true,
                "categoriaId": 1
            }
            """;

        // Act & Assert
        mockMvc.perform(put("/api/produtos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(produtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ração Premium"));

        verify(produtoService, times(1)).atualizar(eq(1L), any(Produto.class));
    }

    @Test
    void testAtualizarEstoque() throws Exception {
        // Arrange
        produto.setQuantidadeEstoque(100);
        when(produtoService.atualizarEstoque(1L, 100)).thenReturn(produto);

        // Act & Assert
        mockMvc.perform(patch("/api/produtos/1/estoque")
                .param("quantidade", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(100));

        verify(produtoService, times(1)).atualizarEstoque(1L, 100);
    }

    @Test
    void testAdicionarEstoque() throws Exception {
        // Arrange
        produto.setQuantidadeEstoque(75);
        when(produtoService.adicionarEstoque(1L, 25)).thenReturn(produto);

        // Act & Assert
        mockMvc.perform(patch("/api/produtos/1/adicionar-estoque")
                .param("quantidade", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(75));

        verify(produtoService, times(1)).adicionarEstoque(1L, 25);
    }

    @Test
    void testAtivarProduto() throws Exception {
        // Arrange
        doNothing().when(produtoService).ativar(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/produtos/1/ativar"))
                .andExpect(status().isNoContent());

        verify(produtoService, times(1)).ativar(1L);
    }

    @Test
    void testDesativarProduto() throws Exception {
        // Arrange
        doNothing().when(produtoService).desativar(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/produtos/1/desativar"))
                .andExpect(status().isNoContent());

        verify(produtoService, times(1)).desativar(1L);
    }

    @Test
    void testDeletarProduto() throws Exception {
        // Arrange
        doNothing().when(produtoService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoService, times(1)).deletar(1L);
    }
}
