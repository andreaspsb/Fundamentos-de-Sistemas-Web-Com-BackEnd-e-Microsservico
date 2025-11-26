package com.petshop.service;

import com.petshop.model.Cliente;
import com.petshop.model.Pet;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.PetRepository;
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
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private PetService petService;

    private Pet pet;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("12345678901");

        pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        pet.setTipo("cao");
        pet.setRaca("Labrador");
        pet.setIdade(3);
        pet.setPeso(25.0);
        pet.setSexo("M");
        pet.setCastrado(true);
        pet.setTemAlergia(false);
        pet.setPrecisaMedicacao(false);
        pet.setComportamentoAgressivo(false);
        pet.setCliente(cliente);
    }

    @Test
    void testListarTodos() {
        // Arrange
        List<Pet> pets = Arrays.asList(pet);
        when(petRepository.findAll()).thenReturn(pets);

        // Act
        List<Pet> resultado = petService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(petRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        // Act
        Optional<Pet> resultado = petService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Rex", resultado.get().getNome());
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Pet> resultado = petService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(petRepository, times(1)).findById(999L);
    }

    @Test
    void testBuscarPorCliente() {
        // Arrange
        List<Pet> pets = Arrays.asList(pet);
        when(petRepository.findByClienteId(1L)).thenReturn(pets);

        // Act
        List<Pet> resultado = petService.buscarPorCliente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(petRepository, times(1)).findByClienteId(1L);
    }

    @Test
    void testBuscarPorTipo() {
        // Arrange
        List<Pet> pets = Arrays.asList(pet);
        when(petRepository.findByTipo("cao")).thenReturn(pets);

        // Act
        List<Pet> resultado = petService.buscarPorTipo("cao");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("cao", resultado.get(0).getTipo());
        verify(petRepository, times(1)).findByTipo("cao");
    }

    @Test
    void testSalvarComSucesso() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        // Act
        Pet resultado = petService.salvar(pet, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Rex", resultado.getNome());
        assertEquals(cliente, resultado.getCliente());
        verify(clienteRepository, times(1)).findById(1L);
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void testSalvarClienteNaoEncontrado() {
        // Arrange
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            petService.salvar(pet, 999L);
        });

        assertEquals("Cliente não encontrado com ID: 999", exception.getMessage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void testAtualizar() {
        // Arrange
        Pet petAtualizado = new Pet();
        petAtualizado.setNome("Rex Jr");
        petAtualizado.setTipo("cao");
        petAtualizado.setRaca("Golden Retriever");
        petAtualizado.setIdade(4);
        petAtualizado.setPeso(28.0);
        petAtualizado.setSexo("M");
        petAtualizado.setCastrado(true);
        petAtualizado.setTemAlergia(true);
        petAtualizado.setPrecisaMedicacao(true);
        petAtualizado.setComportamentoAgressivo(false);
        petAtualizado.setObservacoes("Precisa de cuidados especiais");

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        // Act
        Pet resultado = petService.atualizar(1L, petAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Rex Jr", resultado.getNome());
        assertEquals("Golden Retriever", resultado.getRaca());
        assertEquals(4, resultado.getIdade());
        assertTrue(resultado.getTemAlergia());
        assertTrue(resultado.getPrecisaMedicacao());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void testAtualizarNaoEncontrado() {
        // Arrange
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            petService.atualizar(999L, pet);
        });

        assertEquals("Pet não encontrado com ID: 999", exception.getMessage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void testDeletar() {
        // Arrange
        when(petRepository.existsById(1L)).thenReturn(true);
        doNothing().when(petRepository).deleteById(1L);

        // Act
        petService.deletar(1L);

        // Assert
        verify(petRepository, times(1)).existsById(1L);
        verify(petRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarNaoEncontrado() {
        // Arrange
        when(petRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            petService.deletar(999L);
        });

        assertEquals("Pet não encontrado com ID: 999", exception.getMessage());
        verify(petRepository, never()).deleteById(anyLong());
    }
}
