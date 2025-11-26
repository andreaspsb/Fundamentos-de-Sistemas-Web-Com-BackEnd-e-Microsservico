package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.model.Cliente;
import com.petshop.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ClienteController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    void deveListarTodosClientes() throws Exception {
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNome("João Silva");
        cliente1.setEmail("joao@test.com");
        cliente1.setCpf("12345678901");
        cliente1.setTelefone("11999999999");
        cliente1.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente1.setSexo("M");
        cliente1.setEndereco("Rua Teste");
        cliente1.setNumero("123");
        cliente1.setBairro("Centro");
        cliente1.setCidade("São Paulo");
        
        List<Cliente> clientes = Arrays.asList(cliente1);
        
        when(clienteService.listarTodos()).thenReturn(clientes);
        
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveBuscarClientePorId() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@test.com");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente.setSexo("M");
        cliente.setEndereco("Rua Teste");
        cliente.setNumero("123");
        cliente.setBairro("Centro");
        cliente.setCidade("São Paulo");
        
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    void deveCriarCliente() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("João Silva");
        cliente.setEmail("joao@test.com");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente.setSexo("M");
        cliente.setEndereco("Rua Teste");
        cliente.setNumero("123");
        cliente.setBairro("Centro");
        cliente.setCidade("São Paulo");
        
        Cliente clienteSalvo = new Cliente();
        clienteSalvo.setId(1L);
        clienteSalvo.setNome("João Silva");
        clienteSalvo.setEmail("joao@test.com");
        clienteSalvo.setCpf("12345678901");
        clienteSalvo.setTelefone("11999999999");
        clienteSalvo.setDataNascimento(LocalDate.of(1990, 1, 1));
        clienteSalvo.setSexo("M");
        clienteSalvo.setEndereco("Rua Teste");
        clienteSalvo.setNumero("123");
        clienteSalvo.setBairro("Centro");
        clienteSalvo.setCidade("São Paulo");
        
        when(clienteService.salvar(any(Cliente.class))).thenReturn(clienteSalvo);
        
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveAtualizarCliente() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("João Silva Atualizado");
        cliente.setEmail("joao@test.com");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");
        cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
        cliente.setSexo("M");
        cliente.setEndereco("Rua Teste");
        cliente.setNumero("123");
        cliente.setBairro("Centro");
        cliente.setCidade("São Paulo");
        
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setId(1L);
        clienteAtualizado.setNome("João Silva Atualizado");
        clienteAtualizado.setEmail("joao@test.com");
        clienteAtualizado.setCpf("12345678901");
        clienteAtualizado.setTelefone("11999999999");
        clienteAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));
        clienteAtualizado.setSexo("M");
        clienteAtualizado.setEndereco("Rua Teste");
        clienteAtualizado.setNumero("123");
        clienteAtualizado.setBairro("Centro");
        clienteAtualizado.setCidade("São Paulo");
        
        when(clienteService.atualizar(eq(1L), any(Cliente.class))).thenReturn(clienteAtualizado);
        
        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarCliente() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }
}
