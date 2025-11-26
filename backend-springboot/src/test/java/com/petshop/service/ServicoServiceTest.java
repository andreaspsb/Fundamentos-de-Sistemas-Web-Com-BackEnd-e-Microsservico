package com.petshop.service;

import com.petshop.model.Servico;
import com.petshop.repository.ServicoRepository;
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
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private Servico servico;

    @BeforeEach
    void setUp() {
        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Banho");
        servico.setDescricao("Banho completo com secagem");
        servico.setPreco(50.0);
        servico.setAtivo(true);
    }

    @Test
    void testListarTodos() {
        // Arrange
        List<Servico> servicos = Arrays.asList(servico);
        when(servicoRepository.findAll()).thenReturn(servicos);

        // Act
        List<Servico> resultado = servicoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(servicoRepository, times(1)).findAll();
    }

    @Test
    void testListarAtivos() {
        // Arrange
        List<Servico> servicos = Arrays.asList(servico);
        when(servicoRepository.findByAtivo(true)).thenReturn(servicos);

        // Act
        List<Servico> resultado = servicoService.listarAtivos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
        verify(servicoRepository, times(1)).findByAtivo(true);
    }

    @Test
    void testBuscarPorId() {
        // Arrange
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        // Act
        Optional<Servico> resultado = servicoService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Banho", resultado.get().getNome());
        verify(servicoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Servico> resultado = servicoService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(servicoRepository, times(1)).findById(999L);
    }

    @Test
    void testBuscarPorNome() {
        // Arrange
        when(servicoRepository.findByNome("Banho")).thenReturn(Optional.of(servico));

        // Act
        Optional<Servico> resultado = servicoService.buscarPorNome("Banho");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Banho", resultado.get().getNome());
        verify(servicoRepository, times(1)).findByNome("Banho");
    }

    @Test
    void testSalvarComSucesso() {
        // Arrange
        when(servicoRepository.existsByNome("Banho")).thenReturn(false);
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // Act
        Servico resultado = servicoService.salvar(servico);

        // Assert
        assertNotNull(resultado);
        assertEquals("Banho", resultado.getNome());
        verify(servicoRepository, times(1)).existsByNome("Banho");
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    void testSalvarNomeDuplicado() {
        // Arrange
        when(servicoRepository.existsByNome("Banho")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.salvar(servico);
        });

        assertEquals("Já existe um serviço com este nome", exception.getMessage());
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    void testAtualizar() {
        // Arrange
        Servico servicoAtualizado = new Servico();
        servicoAtualizado.setNome("Banho Premium");
        servicoAtualizado.setDescricao("Banho premium");
        servicoAtualizado.setPreco(75.0);
        servicoAtualizado.setAtivo(true);

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNome("Banho Premium")).thenReturn(false);
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // Act
        Servico resultado = servicoService.atualizar(1L, servicoAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Banho Premium", resultado.getNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    void testAtualizarNaoEncontrado() {
        // Arrange
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.atualizar(999L, servico);
        });

        assertEquals("Serviço não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testAtualizarNomeDuplicado() {
        // Arrange
        Servico servicoAtualizado = new Servico();
        servicoAtualizado.setNome("Tosa");
        servicoAtualizado.setPreco(60.0);
        servicoAtualizado.setAtivo(true);

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNome("Tosa")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.atualizar(1L, servicoAtualizado);
        });

        assertEquals("Já existe um serviço com este nome", exception.getMessage());
    }

    @Test
    void testAtivar() {
        // Arrange
        servico.setAtivo(false);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // Act
        servicoService.ativar(1L);

        // Assert
        assertTrue(servico.getAtivo());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    void testAtivarNaoEncontrado() {
        // Arrange
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.ativar(999L);
        });

        assertEquals("Serviço não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testDesativar() {
        // Arrange
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // Act
        servicoService.desativar(1L);

        // Assert
        assertFalse(servico.getAtivo());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    void testDesativarNaoEncontrado() {
        // Arrange
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.desativar(999L);
        });

        assertEquals("Serviço não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testDeletar() {
        // Arrange
        when(servicoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(servicoRepository).deleteById(1L);

        // Act
        servicoService.deletar(1L);

        // Assert
        verify(servicoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarNaoEncontrado() {
        // Arrange
        when(servicoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicoService.deletar(999L);
        });

        assertEquals("Serviço não encontrado com ID: 999", exception.getMessage());
        verify(servicoRepository, never()).deleteById(anyLong());
    }
}
