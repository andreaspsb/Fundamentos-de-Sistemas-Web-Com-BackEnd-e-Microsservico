package com.petshop.service;

import com.petshop.model.Categoria;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Ração");
        categoria.setDescricao("Rações para pets");
        categoria.setAtivo(true);
    }

    @Test
    void testListarTodas() {
        // Arrange
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        // Act
        List<Categoria> resultado = categoriaService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void testListarAtivas() {
        // Arrange
        List<Categoria> categorias = Arrays.asList(categoria);
        when(categoriaRepository.findByAtivo(true)).thenReturn(categorias);

        // Act
        List<Categoria> resultado = categoriaService.listarAtivas();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
        verify(categoriaRepository, times(1)).findByAtivo(true);
    }

    @Test
    void testBuscarPorId() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        // Act
        Optional<Categoria> resultado = categoriaService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Ração", resultado.get().getNome());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Categoria> resultado = categoriaService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(categoriaRepository, times(1)).findById(999L);
    }

    @Test
    void testBuscarPorNome() {
        // Arrange
        when(categoriaRepository.findByNome("Ração")).thenReturn(Optional.of(categoria));

        // Act
        Optional<Categoria> resultado = categoriaService.buscarPorNome("Ração");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Ração", resultado.get().getNome());
        verify(categoriaRepository, times(1)).findByNome("Ração");
    }

    @Test
    void testSalvarComSucesso() {
        // Arrange
        when(categoriaRepository.existsByNome("Ração")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act
        Categoria resultado = categoriaService.salvar(categoria);

        // Assert
        assertNotNull(resultado);
        assertEquals("Ração", resultado.getNome());
        verify(categoriaRepository, times(1)).existsByNome("Ração");
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void testSalvarNomeDuplicado() {
        // Arrange
        when(categoriaRepository.existsByNome("Ração")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.salvar(categoria);
        });

        assertEquals("Já existe uma categoria com este nome", exception.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void testAtualizar() {
        // Arrange
        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setNome("Ração Premium");
        categoriaAtualizada.setDescricao("Rações premium para pets");
        categoriaAtualizada.setAtivo(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNome("Ração Premium")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act
        Categoria resultado = categoriaService.atualizar(1L, categoriaAtualizada);

        // Assert
        assertNotNull(resultado);
        assertEquals("Ração Premium", resultado.getNome());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void testAtualizarMesmoNome() {
        // Arrange - Updating without changing the name
        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setNome("Ração");
        categoriaAtualizada.setDescricao("Descrição atualizada");
        categoriaAtualizada.setAtivo(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act
        Categoria resultado = categoriaService.atualizar(1L, categoriaAtualizada);

        // Assert
        assertNotNull(resultado);
        // Should not check for duplicate since name is the same
        verify(categoriaRepository, never()).existsByNome(anyString());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void testAtualizarNaoEncontrado() {
        // Arrange
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.atualizar(999L, categoria);
        });

        assertEquals("Categoria não encontrada com ID: 999", exception.getMessage());
    }

    @Test
    void testAtualizarNomeDuplicado() {
        // Arrange
        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setNome("Brinquedos");
        categoriaAtualizada.setDescricao("Brinquedos para pets");
        categoriaAtualizada.setAtivo(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNome("Brinquedos")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.atualizar(1L, categoriaAtualizada);
        });

        assertEquals("Já existe uma categoria com este nome", exception.getMessage());
    }

    @Test
    void testAtivar() {
        // Arrange
        categoria.setAtivo(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act
        categoriaService.ativar(1L);

        // Assert
        assertTrue(categoria.getAtivo());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void testAtivarNaoEncontrado() {
        // Arrange
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.ativar(999L);
        });

        assertEquals("Categoria não encontrada com ID: 999", exception.getMessage());
    }

    @Test
    void testDesativar() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        // Act
        categoriaService.desativar(1L);

        // Assert
        assertFalse(categoria.getAtivo());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void testDesativarNaoEncontrado() {
        // Arrange
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.desativar(999L);
        });

        assertEquals("Categoria não encontrada com ID: 999", exception.getMessage());
    }

    @Test
    void testDeletar() {
        // Arrange
        when(categoriaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoriaRepository).deleteById(1L);

        // Act
        categoriaService.deletar(1L);

        // Assert
        verify(categoriaRepository, times(1)).existsById(1L);
        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarNaoEncontrado() {
        // Arrange
        when(categoriaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.deletar(999L);
        });

        assertEquals("Categoria não encontrada com ID: 999", exception.getMessage());
        verify(categoriaRepository, never()).deleteById(anyLong());
    }
}
