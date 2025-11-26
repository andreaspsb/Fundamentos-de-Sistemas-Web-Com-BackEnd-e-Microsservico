package com.petshop.controller;

import com.petshop.model.Servico;
import com.petshop.service.ServicoService;
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

@WebMvcTest(value = ServicoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class ServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicoService servicoService;

    private Servico servico;
    private Servico servicoTosa;

    @BeforeEach
    void setUp() {
        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Banho");
        servico.setDescricao("Banho completo com shampoo especial");
        servico.setPreco(50.0);
        servico.setAtivo(true);

        servicoTosa = new Servico();
        servicoTosa.setId(2L);
        servicoTosa.setNome("Tosa");
        servicoTosa.setDescricao("Tosa higiênica ou completa");
        servicoTosa.setPreco(70.0);
        servicoTosa.setAtivo(true);
    }

    // ========== Testes de Listagem ==========

    @Test
    void testListarTodos() throws Exception {
        // Arrange
        List<Servico> servicos = Arrays.asList(servico, servicoTosa);
        when(servicoService.listarTodos()).thenReturn(servicos);

        // Act & Assert
        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Banho"))
                .andExpect(jsonPath("$[0].preco").value(50.0))
                .andExpect(jsonPath("$[0].ativo").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nome").value("Tosa"));

        verify(servicoService, times(1)).listarTodos();
    }

    @Test
    void testListarAtivos() throws Exception {
        // Arrange
        List<Servico> servicos = Arrays.asList(servico, servicoTosa);
        when(servicoService.listarAtivos()).thenReturn(servicos);

        // Act & Assert
        mockMvc.perform(get("/api/servicos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ativo").value(true))
                .andExpect(jsonPath("$[1].ativo").value(true));

        verify(servicoService, times(1)).listarAtivos();
    }

    @Test
    void testBuscarPorIdExistente() throws Exception {
        // Arrange
        when(servicoService.buscarPorId(1L)).thenReturn(Optional.of(servico));

        // Act & Assert
        mockMvc.perform(get("/api/servicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Banho"))
                .andExpect(jsonPath("$.descricao").value("Banho completo com shampoo especial"))
                .andExpect(jsonPath("$.preco").value(50.0))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(servicoService, times(1)).buscarPorId(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(servicoService.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/servicos/999"))
                .andExpect(status().isNotFound());

        verify(servicoService, times(1)).buscarPorId(999L);
    }

    // ========== Testes de Criação ==========

    @Test
    void testCriarServico() throws Exception {
        // Arrange
        when(servicoService.salvar(any(Servico.class))).thenReturn(servico);

        String servicoJson = """
            {
                "nome": "Banho",
                "descricao": "Banho completo com shampoo especial",
                "preco": 50.0,
                "ativo": true
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Banho"))
                .andExpect(jsonPath("$.preco").value(50.0));

        verify(servicoService, times(1)).salvar(any(Servico.class));
    }

    @Test
    void testCriarServicoTosa() throws Exception {
        // Arrange
        when(servicoService.salvar(any(Servico.class))).thenReturn(servicoTosa);

        String servicoJson = """
            {
                "nome": "Tosa",
                "descricao": "Tosa higiênica ou completa",
                "preco": 70.0,
                "ativo": true
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Tosa"))
                .andExpect(jsonPath("$.preco").value(70.0));

        verify(servicoService, times(1)).salvar(any(Servico.class));
    }

    // ========== Testes de Atualização ==========

    @Test
    void testAtualizarServico() throws Exception {
        // Arrange
        servico.setNome("Banho Premium");
        servico.setPreco(60.0);
        when(servicoService.atualizar(eq(1L), any(Servico.class))).thenReturn(servico);

        String servicoJson = """
            {
                "nome": "Banho Premium",
                "descricao": "Banho premium com hidratação",
                "preco": 60.0,
                "ativo": true
            }
            """;

        // Act & Assert
        mockMvc.perform(put("/api/servicos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Banho Premium"))
                .andExpect(jsonPath("$.preco").value(60.0));

        verify(servicoService, times(1)).atualizar(eq(1L), any(Servico.class));
    }

    // ========== Testes de Ativação/Desativação ==========

    @Test
    void testAtivarServico() throws Exception {
        // Arrange
        doNothing().when(servicoService).ativar(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/servicos/1/ativar"))
                .andExpect(status().isNoContent());

        verify(servicoService, times(1)).ativar(1L);
    }

    @Test
    void testDesativarServico() throws Exception {
        // Arrange
        doNothing().when(servicoService).desativar(1L);

        // Act & Assert
        mockMvc.perform(patch("/api/servicos/1/desativar"))
                .andExpect(status().isNoContent());

        verify(servicoService, times(1)).desativar(1L);
    }

    // ========== Testes de Deleção ==========

    @Test
    void testDeletarServico() throws Exception {
        // Arrange
        doNothing().when(servicoService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/servicos/1"))
                .andExpect(status().isNoContent());

        verify(servicoService, times(1)).deletar(1L);
    }

    // ========== Testes de Diferentes Serviços ==========

    @Test
    void testListarVariosServicos() throws Exception {
        // Arrange
        Servico consulta = new Servico();
        consulta.setId(3L);
        consulta.setNome("Consulta Veterinária");
        consulta.setDescricao("Consulta com veterinário");
        consulta.setPreco(120.0);
        consulta.setAtivo(true);

        List<Servico> servicos = Arrays.asList(servico, servicoTosa, consulta);
        when(servicoService.listarTodos()).thenReturn(servicos);

        // Act & Assert
        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[2].nome").value("Consulta Veterinária"))
                .andExpect(jsonPath("$[2].preco").value(120.0));

        verify(servicoService, times(1)).listarTodos();
    }

    @Test
    void testListarAtivosSemInativos() throws Exception {
        // Arrange
        servicoTosa.setAtivo(false);
        List<Servico> ativos = Arrays.asList(servico);
        when(servicoService.listarAtivos()).thenReturn(ativos);

        // Act & Assert
        mockMvc.perform(get("/api/servicos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Banho"));

        verify(servicoService, times(1)).listarAtivos();
    }

    @Test
    void testBuscarTosaPorId() throws Exception {
        // Arrange
        when(servicoService.buscarPorId(2L)).thenReturn(Optional.of(servicoTosa));

        // Act & Assert
        mockMvc.perform(get("/api/servicos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Tosa"))
                .andExpect(jsonPath("$.descricao").value("Tosa higiênica ou completa"))
                .andExpect(jsonPath("$.preco").value(70.0));

        verify(servicoService, times(1)).buscarPorId(2L);
    }

    // ========== Testes de Serviços com Preços Especiais ==========

    @Test
    void testServicoComPrecoAlto() throws Exception {
        // Arrange
        Servico hospedagem = new Servico();
        hospedagem.setId(4L);
        hospedagem.setNome("Hospedagem");
        hospedagem.setDescricao("Hospedagem diária para pets");
        hospedagem.setPreco(150.0);
        hospedagem.setAtivo(true);

        when(servicoService.buscarPorId(4L)).thenReturn(Optional.of(hospedagem));

        // Act & Assert
        mockMvc.perform(get("/api/servicos/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Hospedagem"))
                .andExpect(jsonPath("$.preco").value(150.0));

        verify(servicoService, times(1)).buscarPorId(4L);
    }

    @Test
    void testCriarServicoSemDescricao() throws Exception {
        // Arrange
        Servico simples = new Servico();
        simples.setId(5L);
        simples.setNome("Hidratação");
        simples.setPreco(30.0);
        simples.setAtivo(true);

        when(servicoService.salvar(any(Servico.class))).thenReturn(simples);

        String servicoJson = """
            {
                "nome": "Hidratação",
                "preco": 30.0,
                "ativo": true
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(servicoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Hidratação"))
                .andExpect(jsonPath("$.preco").value(30.0));

        verify(servicoService, times(1)).salvar(any(Servico.class));
    }
}
