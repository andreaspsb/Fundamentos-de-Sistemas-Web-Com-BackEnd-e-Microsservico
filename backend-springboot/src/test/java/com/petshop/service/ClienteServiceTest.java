package com.petshop.service;

import com.petshop.model.Cliente;
import com.petshop.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11987654321");
        cliente.setEmail("joao@example.com");
        cliente.setDataNascimento(LocalDate.of(1990, 5, 15));
        cliente.setSexo("M");
        cliente.setEndereco("Rua Teste");
        cliente.setNumero("123");
        cliente.setBairro("Centro");
        cliente.setCidade("São Paulo");
    }

    @Test
    void testListarTodos() {
        // Arrange
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        List<Cliente> resultado = clienteService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        Optional<Cliente> resultado = clienteService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Cliente> resultado = clienteService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(clienteRepository, times(1)).findById(999L);
    }

    @Test
    void testBuscarPorCpf() {
        // Arrange
        when(clienteRepository.findByCpf("12345678901")).thenReturn(Optional.of(cliente));

        // Act
        Optional<Cliente> resultado = clienteService.buscarPorCpf("12345678901");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(clienteRepository, times(1)).findByCpf("12345678901");
    }

    @Test
    void testBuscarPorEmail() {
        // Arrange
        when(clienteRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(cliente));

        // Act
        Optional<Cliente> resultado = clienteService.buscarPorEmail("joao@example.com");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(clienteRepository, times(1)).findByEmail("joao@example.com");
    }

    @Test
    void testSalvarComSucesso() {
        // Arrange
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.salvar(cliente);

        // Assert
        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("12345678901", resultado.getCpf()); // CPF limpo
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testSalvarComCpfFormatado() {
        // Arrange
        cliente.setCpf("123.456.789-01");
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.salvar(cliente);

        // Assert
        assertNotNull(resultado);
        assertEquals("12345678901", resultado.getCpf()); // CPF sem formatação
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testSalvarComCpfDuplicado() {
        // Arrange
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(cliente);
        });
        
        assertEquals("CPF já cadastrado", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testSalvarComEmailDuplicado() {
        // Arrange
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(cliente);
        });
        
        assertEquals("Email já cadastrado", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testSalvarComCpfInvalido() {
        // Arrange
        cliente.setCpf("123"); // CPF inválido

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(cliente);
        });
        
        assertEquals("CPF inválido", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testSalvarComTelefoneInvalido() {
        // Arrange
        cliente.setTelefone("123"); // Telefone inválido

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.salvar(cliente);
        });
        
        assertEquals("Telefone inválido", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testAtualizar() {
        // Arrange
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setNome("João Silva Santos");
        clienteAtualizado.setCpf("12345678901");
        clienteAtualizado.setTelefone("11999887766");
        clienteAtualizado.setEmail("joao@example.com");
        clienteAtualizado.setDataNascimento(LocalDate.of(1990, 5, 15));
        clienteAtualizado.setSexo("M");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        Cliente resultado = clienteService.atualizar(1L, clienteAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("João Silva Santos", resultado.getNome());
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testAtualizarClienteNaoEncontrado() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.atualizar(999L, cliente);
        });
        
        assertEquals("Cliente não encontrado com ID: 999", exception.getMessage());
        verify(clienteRepository, times(1)).findById(999L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testAtualizarComCpfDuplicado() {
        // Arrange
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setCpf("98765432100"); // CPF diferente
        clienteAtualizado.setEmail("joao@example.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpf("98765432100")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.atualizar(1L, clienteAtualizado);
        });
        
        assertEquals("CPF já cadastrado", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void testDeletar() {
        // Arrange
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        // Act
        clienteService.deletar(1L);

        // Assert
        verify(clienteRepository, times(1)).existsById(1L);
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarClienteNaoEncontrado() {
        // Arrange
        when(clienteRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteService.deletar(999L);
        });
        
        assertEquals("Cliente não encontrado com ID: 999", exception.getMessage());
        verify(clienteRepository, times(1)).existsById(999L);
        verify(clienteRepository, never()).deleteById(anyLong());
    }
}
