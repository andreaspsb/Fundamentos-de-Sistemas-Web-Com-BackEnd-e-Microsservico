package com.petshop.service;

import com.petshop.model.Produto;
import com.petshop.model.Categoria;
import com.petshop.repository.ProdutoRepository;
import com.petshop.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
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
    void testListarTodos() {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoRepository.findAll()).thenReturn(produtos);

        // Act
        List<Produto> resultado = produtoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ração Premium", resultado.get(0).getNome());
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    void testListarDisponiveis() {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoRepository.findProdutosDisponiveis()).thenReturn(produtos);

        // Act
        List<Produto> resultado = produtoService.listarDisponiveis();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
        verify(produtoRepository, times(1)).findProdutosDisponiveis();
    }

    @Test
    void testBuscarPorId() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act
        Optional<Produto> resultado = produtoService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Ração Premium", resultado.get().getNome());
        verify(produtoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Produto> resultado = produtoService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(produtoRepository, times(1)).findById(999L);
    }

    @Test
    void testSalvarComSucesso() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.salvar(produto, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Ração Premium", resultado.getNome());
        assertEquals(categoria, resultado.getCategoria());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testSalvarComCategoriaInexistente() {
        // Arrange
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.salvar(produto, 999L);
        });
        
        assertEquals("Categoria não encontrada com ID: 999", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(999L);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void testAtualizar() {
        // Arrange
        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setNome("Ração Premium Plus");
        produtoAtualizado.setDescricao("Nova descrição");
        produtoAtualizado.setPreco(99.90);
        produtoAtualizado.setQuantidadeEstoque(60);
        produtoAtualizado.setAtivo(true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.atualizar(1L, produtoAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Ração Premium Plus", resultado.getNome());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testAtualizarProdutoNaoEncontrado() {
        // Arrange
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.atualizar(999L, produto);
        });
        
        assertEquals("Produto não encontrado com ID: 999", exception.getMessage());
        verify(produtoRepository, times(1)).findById(999L);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void testAtualizarEstoque() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.atualizarEstoque(1L, 100);

        // Assert
        assertNotNull(resultado);
        assertEquals(100, resultado.getQuantidadeEstoque());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testAdicionarEstoque() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.adicionarEstoque(1L, 25);

        // Assert
        assertNotNull(resultado);
        assertEquals(75, resultado.getQuantidadeEstoque()); // 50 + 25
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testReduzirEstoqueComSucesso() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto resultado = produtoService.reduzirEstoque(1L, 20);

        // Assert
        assertNotNull(resultado);
        assertEquals(30, resultado.getQuantidadeEstoque()); // 50 - 20
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testReduzirEstoqueInsuficiente() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.reduzirEstoque(1L, 100); // Tentando reduzir mais que o disponível
        });
        
        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void testAtivar() {
        // Arrange
        produto.setAtivo(false);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        produtoService.ativar(1L);

        // Assert
        assertTrue(produto.getAtivo());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testDesativar() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        produtoService.desativar(1L);

        // Assert
        assertFalse(produto.getAtivo());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void testDeletar() {
        // Arrange
        when(produtoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(produtoRepository).deleteById(1L);

        // Act
        produtoService.deletar(1L);

        // Assert
        verify(produtoRepository, times(1)).existsById(1L);
        verify(produtoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarProdutoNaoEncontrado() {
        // Arrange
        when(produtoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            produtoService.deletar(999L);
        });
        
        assertEquals("Produto não encontrado com ID: 999", exception.getMessage());
        verify(produtoRepository, times(1)).existsById(999L);
        verify(produtoRepository, never()).deleteById(anyLong());
    }

    @Test
    void testBuscarPorNome() {
        // Arrange
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoRepository.buscarPorNome("Ração")).thenReturn(produtos);

        // Act
        List<Produto> resultado = produtoService.buscarPorNome("Ração");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNome().contains("Ração"));
        verify(produtoRepository, times(1)).buscarPorNome("Ração");
    }

    @Test
    void testListarEstoqueBaixo() {
        // Arrange
        produto.setQuantidadeEstoque(5);
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoRepository.findByQuantidadeEstoqueLessThan(10)).thenReturn(produtos);

        // Act
        List<Produto> resultado = produtoService.listarEstoqueBaixo(10);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getQuantidadeEstoque() < 10);
        verify(produtoRepository, times(1)).findByQuantidadeEstoqueLessThan(10);
    }
}
