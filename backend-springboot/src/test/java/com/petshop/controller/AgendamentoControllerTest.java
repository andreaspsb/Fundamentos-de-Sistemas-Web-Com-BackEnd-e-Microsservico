package com.petshop.controller;

import com.petshop.model.Agendamento;
import com.petshop.model.Agendamento.StatusAgendamento;
import com.petshop.model.Cliente;
import com.petshop.model.Pet;
import com.petshop.model.Servico;
import com.petshop.service.AgendamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AgendamentoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendamentoService agendamentoService;

    private Agendamento agendamento;
    private Cliente cliente;
    private Pet pet;
    private Servico servico;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria Santos");
        cliente.setTelefone("11988888888");

        pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        pet.setTipo("cao");
        pet.setCliente(cliente);

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Banho");
        servico.setDescricao("Banho completo");
        servico.setPreco(50.0);
        servico.setAtivo(true);

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setDataAgendamento(LocalDate.of(2025, 1, 15));
        agendamento.setHorario(LocalTime.of(10, 0));
        agendamento.setMetodoAtendimento("local");
        agendamento.setPortePet("medio");
        agendamento.setObservacoes("Pet sensível");
        agendamento.setValorTotal(50.0);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setCliente(cliente);
        agendamento.setPet(pet);
        agendamento.getServicos().add(servico);
    }

    // ========== Testes de Listagem ==========

    @Test
    void testListarTodos() throws Exception {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoService.listarTodos()).thenReturn(agendamentos);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].metodoAtendimento").value("local"))
                .andExpect(jsonPath("$[0].portePet").value("medio"))
                .andExpect(jsonPath("$[0].valorTotal").value(50.0))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].clienteNome").value("Maria Santos"))
                .andExpect(jsonPath("$[0].petNome").value("Rex"));

        verify(agendamentoService, times(1)).listarTodos();
    }

    @Test
    void testBuscarPorIdExistente() throws Exception {
        // Arrange
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.metodoAtendimento").value("local"))
                .andExpect(jsonPath("$.valorTotal").value(50.0));

        verify(agendamentoService, times(1)).buscarPorId(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(agendamentoService.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/999"))
                .andExpect(status().isNotFound());

        verify(agendamentoService, times(1)).buscarPorId(999L);
    }

    @Test
    void testBuscarPorCliente() throws Exception {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoService.buscarPorCliente(1L)).thenReturn(agendamentos);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(1))
                .andExpect(jsonPath("$[0].clienteNome").value("Maria Santos"));

        verify(agendamentoService, times(1)).buscarPorCliente(1L);
    }

    @Test
    void testBuscarPorData() throws Exception {
        // Arrange
        LocalDate data = LocalDate.of(2025, 1, 15);
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoService.buscarPorData(data)).thenReturn(agendamentos);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/data/2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(agendamentoService, times(1)).buscarPorData(data);
    }

    @Test
    void testBuscarPorStatus() throws Exception {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoService.buscarPorStatus(StatusAgendamento.PENDENTE)).thenReturn(agendamentos);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/status/PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));

        verify(agendamentoService, times(1)).buscarPorStatus(StatusAgendamento.PENDENTE);
    }

    @Test
    void testVerificarDisponibilidade() throws Exception {
        // Arrange
        LocalDate data = LocalDate.of(2025, 1, 15);
        LocalTime horario = LocalTime.of(14, 0);
        when(agendamentoService.verificarDisponibilidade(data, horario)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/disponibilidade")
                .param("data", "2025-01-15")
                .param("horario", "14:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(agendamentoService, times(1)).verificarDisponibilidade(data, horario);
    }

    @Test
    void testVerificarDisponibilidadeNaoDisponivel() throws Exception {
        // Arrange
        LocalDate data = LocalDate.of(2025, 1, 15);
        LocalTime horario = LocalTime.of(10, 0);
        when(agendamentoService.verificarDisponibilidade(data, horario)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos/disponibilidade")
                .param("data", "2025-01-15")
                .param("horario", "10:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(agendamentoService, times(1)).verificarDisponibilidade(data, horario);
    }

    // ========== Testes de Criação ==========

    @Test
    void testCriarAgendamento() throws Exception {
        // Arrange
        when(agendamentoService.salvar(any(Agendamento.class), eq(1L), eq(1L), anyList()))
                .thenReturn(agendamento);

        String agendamentoJson = """
            {
                "dataAgendamento": "2025-01-15",
                "horario": "10:00:00",
                "metodoAtendimento": "local",
                "portePet": "medio",
                "observacoes": "Pet sensível",
                "clienteId": 1,
                "petId": 1,
                "servicoIds": [1]
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(agendamentoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.metodoAtendimento").value("local"));

        verify(agendamentoService, times(1)).salvar(any(Agendamento.class), eq(1L), eq(1L), anyList());
    }

    // ========== Testes de Atualização de Status ==========

    @Test
    void testAtualizarStatus() throws Exception {
        // Arrange
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        when(agendamentoService.atualizarStatus(1L, StatusAgendamento.CONFIRMADO)).thenReturn(agendamento);

        // Act & Assert
        mockMvc.perform(patch("/api/agendamentos/1/status")
                .param("status", "CONFIRMADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        verify(agendamentoService, times(1)).atualizarStatus(1L, StatusAgendamento.CONFIRMADO);
    }

    @Test
    void testAtualizarStatusEmAndamento() throws Exception {
        // Arrange
        agendamento.setStatus(StatusAgendamento.EM_ANDAMENTO);
        when(agendamentoService.atualizarStatus(1L, StatusAgendamento.EM_ANDAMENTO)).thenReturn(agendamento);

        // Act & Assert
        mockMvc.perform(patch("/api/agendamentos/1/status")
                .param("status", "EM_ANDAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

        verify(agendamentoService, times(1)).atualizarStatus(1L, StatusAgendamento.EM_ANDAMENTO);
    }

    @Test
    void testAtualizarStatusConcluido() throws Exception {
        // Arrange
        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        when(agendamentoService.atualizarStatus(1L, StatusAgendamento.CONCLUIDO)).thenReturn(agendamento);

        // Act & Assert
        mockMvc.perform(patch("/api/agendamentos/1/status")
                .param("status", "CONCLUIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));

        verify(agendamentoService, times(1)).atualizarStatus(1L, StatusAgendamento.CONCLUIDO);
    }

    // ========== Testes de Cancelamento e Deleção ==========

    @Test
    void testCancelarAgendamento() throws Exception {
        // Arrange
        doNothing().when(agendamentoService).cancelar(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/agendamentos/1/cancelar"))
                .andExpect(status().isNoContent());

        verify(agendamentoService, times(1)).cancelar(1L);
    }

    @Test
    void testDeletarAgendamento() throws Exception {
        // Arrange
        doNothing().when(agendamentoService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/agendamentos/1"))
                .andExpect(status().isNoContent());

        verify(agendamentoService, times(1)).deletar(1L);
    }

    // ========== Testes de Serviços no DTO ==========

    @Test
    void testListarComServicos() throws Exception {
        // Arrange
        List<Agendamento> agendamentos = Arrays.asList(agendamento);
        when(agendamentoService.listarTodos()).thenReturn(agendamentos);

        // Act & Assert
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].servicos").isArray())
                .andExpect(jsonPath("$[0].servicos[0].id").value(1))
                .andExpect(jsonPath("$[0].servicos[0].nome").value("Banho"))
                .andExpect(jsonPath("$[0].servicos[0].preco").value(50.0));

        verify(agendamentoService, times(1)).listarTodos();
    }
}
