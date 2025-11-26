package com.petshop.service;

import com.petshop.model.Agendamento;
import com.petshop.model.Agendamento.StatusAgendamento;
import com.petshop.model.Cliente;
import com.petshop.model.Pet;
import com.petshop.model.Servico;
import com.petshop.repository.AgendamentoRepository;
import com.petshop.repository.ClienteRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Agendamento agendamento;
    private Cliente cliente;
    private Pet pet;
    private Servico servico;

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
        pet.setCliente(cliente);

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Banho");
        servico.setPreco(50.0);
        servico.setAtivo(true);
        servico.setAgendamentos(new ArrayList<>());

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setDataAgendamento(LocalDate.now().plusDays(1));
        agendamento.setHorario(LocalTime.of(10, 0));
        agendamento.setMetodoAtendimento("local");
        agendamento.setPortePet("medio");
        agendamento.setValorTotal(50.0);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setCliente(cliente);
        agendamento.setPet(pet);
        agendamento.setServicos(new ArrayList<>());
    }

    // Region: ListarTodos Tests

    @Test
    void testListarTodos() {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoRepository.findAll()).thenReturn(agendamentos);

        // Act
        List<Agendamento> resultado = agendamentoService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository, times(1)).findAll();
    }

    // Region: BuscarPorId Tests

    @Test
    void testBuscarPorId() {
        // Arrange
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        // Act
        Optional<Agendamento> resultado = agendamentoService.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(agendamentoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(agendamentoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Agendamento> resultado = agendamentoService.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(agendamentoRepository, times(1)).findById(999L);
    }

    // Region: BuscarPorCliente Tests

    @Test
    void testBuscarPorCliente() {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoRepository.findByClienteId(1L)).thenReturn(agendamentos);

        // Act
        List<Agendamento> resultado = agendamentoService.buscarPorCliente(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository, times(1)).findByClienteId(1L);
    }

    // Region: BuscarPorData Tests

    @Test
    void testBuscarPorData() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(1);
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoRepository.findByDataAgendamento(data)).thenReturn(agendamentos);

        // Act
        List<Agendamento> resultado = agendamentoService.buscarPorData(data);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository, times(1)).findByDataAgendamento(data);
    }

    // Region: BuscarPorStatus Tests

    @Test
    void testBuscarPorStatus() {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoRepository.findByStatus(StatusAgendamento.PENDENTE)).thenReturn(agendamentos);

        // Act
        List<Agendamento> resultado = agendamentoService.buscarPorStatus(StatusAgendamento.PENDENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(StatusAgendamento.PENDENTE, resultado.get(0).getStatus());
        verify(agendamentoRepository, times(1)).findByStatus(StatusAgendamento.PENDENTE);
    }

    // Region: BuscarPorPeriodo Tests

    @Test
    void testBuscarPorPeriodo() {
        // Arrange
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().plusDays(7);
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoRepository.findByDataAgendamentoBetween(dataInicio, dataFim)).thenReturn(agendamentos);

        // Act
        List<Agendamento> resultado = agendamentoService.buscarPorPeriodo(dataInicio, dataFim);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository, times(1)).findByDataAgendamentoBetween(dataInicio, dataFim);
    }

    // Region: Salvar Tests

    @Test
    void testSalvarComSucesso() {
        // Arrange
        List<Long> servicoIds = Arrays.asList(1L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(servicoRepository.findAllById(servicoIds)).thenReturn(Arrays.asList(servico));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        // Act
        Agendamento resultado = agendamentoService.salvar(agendamento, 1L, 1L, servicoIds);

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusAgendamento.PENDENTE, resultado.getStatus());
        verify(agendamentoRepository, times(2)).save(any(Agendamento.class));
    }

    @Test
    void testSalvarHorarioOcupado() {
        // Arrange
        List<Long> servicoIds = Arrays.asList(1L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.salvar(agendamento, 1L, 1L, servicoIds);
        });

        assertEquals("Horário já está ocupado", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void testSalvarClienteNaoEncontrado() {
        // Arrange
        List<Long> servicoIds = Arrays.asList(1L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.salvar(agendamento, 999L, 1L, servicoIds);
        });

        assertEquals("Cliente não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testSalvarPetNaoEncontrado() {
        // Arrange
        List<Long> servicoIds = Arrays.asList(1L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.salvar(agendamento, 1L, 999L, servicoIds);
        });

        assertEquals("Pet não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    void testSalvarPetNaoPertenceAoCliente() {
        // Arrange
        Cliente outroCliente = new Cliente();
        outroCliente.setId(2L);
        pet.setCliente(outroCliente);

        List<Long> servicoIds = Arrays.asList(1L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.salvar(agendamento, 1L, 1L, servicoIds);
        });

        assertEquals("Pet não pertence ao cliente informado", exception.getMessage());
    }

    @Test
    void testSalvarServicoNaoEncontrado() {
        // Arrange
        List<Long> servicoIds = Arrays.asList(1L, 999L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(servicoRepository.findAllById(servicoIds)).thenReturn(Arrays.asList(servico)); // Retorna só 1

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.salvar(agendamento, 1L, 1L, servicoIds);
        });

        assertEquals("Um ou mais serviços não foram encontrados", exception.getMessage());
    }

    @Test
    void testSalvarComTelebusca() {
        // Arrange
        agendamento.setMetodoAtendimento("telebusca");
        List<Long> servicoIds = Arrays.asList(1L);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(servicoRepository.findAllById(servicoIds)).thenReturn(Arrays.asList(servico));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> {
            Agendamento saved = invocation.getArgument(0);
            // Taxa de telebusca deve ser adicionada (50 + 20 = 70)
            assertTrue(saved.getValorTotal() >= 70.0);
            return saved;
        });

        // Act
        Agendamento resultado = agendamentoService.salvar(agendamento, 1L, 1L, servicoIds);

        // Assert
        assertNotNull(resultado);
    }

    // Region: AtualizarStatus Tests

    @Test
    void testAtualizarStatus() {
        // Arrange
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        // Act
        Agendamento resultado = agendamentoService.atualizarStatus(1L, StatusAgendamento.CONFIRMADO);

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusAgendamento.CONFIRMADO, resultado.getStatus());
        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
    }

    @Test
    void testAtualizarStatusNaoEncontrado() {
        // Arrange
        when(agendamentoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.atualizarStatus(999L, StatusAgendamento.CONFIRMADO);
        });

        assertEquals("Agendamento não encontrado com ID: 999", exception.getMessage());
    }

    // Region: Cancelar Tests

    @Test
    void testCancelar() {
        // Arrange
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        // Act
        agendamentoService.cancelar(1L);

        // Assert
        assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
    }

    @Test
    void testCancelarNaoEncontrado() {
        // Arrange
        when(agendamentoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.cancelar(999L);
        });

        assertEquals("Agendamento não encontrado com ID: 999", exception.getMessage());
    }

    // Region: Deletar Tests

    @Test
    void testDeletar() {
        // Arrange
        when(agendamentoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(agendamentoRepository).deleteById(1L);

        // Act
        agendamentoService.deletar(1L);

        // Assert
        verify(agendamentoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarNaoEncontrado() {
        // Arrange
        when(agendamentoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            agendamentoService.deletar(999L);
        });

        assertEquals("Agendamento não encontrado com ID: 999", exception.getMessage());
        verify(agendamentoRepository, never()).deleteById(anyLong());
    }

    // Region: VerificarDisponibilidade Tests

    @Test
    void testVerificarDisponibilidadeDisponivel() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime horario = LocalTime.of(14, 0);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(data, horario)).thenReturn(false);

        // Act
        boolean resultado = agendamentoService.verificarDisponibilidade(data, horario);

        // Assert
        assertTrue(resultado);
        verify(agendamentoRepository, times(1)).existsByDataAgendamentoAndHorario(data, horario);
    }

    @Test
    void testVerificarDisponibilidadeOcupado() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime horario = LocalTime.of(10, 0);
        when(agendamentoRepository.existsByDataAgendamentoAndHorario(data, horario)).thenReturn(true);

        // Act
        boolean resultado = agendamentoService.verificarDisponibilidade(data, horario);

        // Assert
        assertFalse(resultado);
        verify(agendamentoRepository, times(1)).existsByDataAgendamentoAndHorario(data, horario);
    }
}
