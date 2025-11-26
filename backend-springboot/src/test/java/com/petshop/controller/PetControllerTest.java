package com.petshop.controller;

import com.petshop.model.Cliente;
import com.petshop.model.Pet;
import com.petshop.service.PetService;
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

@WebMvcTest(value = PetController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

    private Pet pet;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Ana Lima");

        pet = new Pet();
        pet.setId(1L);
        pet.setNome("Thor");
        pet.setTipo("cao");
        pet.setRaca("Golden Retriever");
        pet.setIdade(3);
        pet.setPeso(25.0);
        pet.setSexo("M");
        pet.setCastrado(true);
        pet.setObservacoes("Pet calmo e dócil");
        pet.setTemAlergia(false);
        pet.setPrecisaMedicacao(false);
        pet.setComportamentoAgressivo(false);
        pet.setCliente(cliente);
    }

    // ========== Testes de Listagem ==========

    @Test
    void testListarTodos() throws Exception {
        // Arrange
        List<Pet> pets = Arrays.asList(pet);
        when(petService.listarTodos()).thenReturn(pets);

        // Act & Assert
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Thor"))
                .andExpect(jsonPath("$[0].tipo").value("cao"))
                .andExpect(jsonPath("$[0].raca").value("Golden Retriever"))
                .andExpect(jsonPath("$[0].idade").value(3))
                .andExpect(jsonPath("$[0].peso").value(25.0))
                .andExpect(jsonPath("$[0].sexo").value("M"))
                .andExpect(jsonPath("$[0].castrado").value(true))
                .andExpect(jsonPath("$[0].clienteNome").value("Ana Lima"));

        verify(petService, times(1)).listarTodos();
    }

    @Test
    void testBuscarPorIdExistente() throws Exception {
        // Arrange
        when(petService.buscarPorId(1L)).thenReturn(Optional.of(pet));

        // Act & Assert
        mockMvc.perform(get("/api/pets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Thor"))
                .andExpect(jsonPath("$.tipo").value("cao"))
                .andExpect(jsonPath("$.raca").value("Golden Retriever"));

        verify(petService, times(1)).buscarPorId(1L);
    }

    @Test
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(petService.buscarPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/pets/999"))
                .andExpect(status().isNotFound());

        verify(petService, times(1)).buscarPorId(999L);
    }

    @Test
    void testBuscarPorCliente() throws Exception {
        // Arrange
        List<Pet> pets = Arrays.asList(pet);
        when(petService.buscarPorCliente(1L)).thenReturn(pets);

        // Act & Assert
        mockMvc.perform(get("/api/pets/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(1))
                .andExpect(jsonPath("$[0].clienteNome").value("Ana Lima"));

        verify(petService, times(1)).buscarPorCliente(1L);
    }

    @Test
    void testBuscarPorTipo() throws Exception {
        // Arrange
        List<Pet> pets = Arrays.asList(pet);
        when(petService.buscarPorTipo("cao")).thenReturn(pets);

        // Act & Assert
        mockMvc.perform(get("/api/pets/tipo/cao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("cao"));

        verify(petService, times(1)).buscarPorTipo("cao");
    }

    // ========== Testes de Criação ==========

    @Test
    void testCriarPet() throws Exception {
        // Arrange
        when(petService.salvar(any(Pet.class), eq(1L))).thenReturn(pet);

        String petJson = """
            {
                "nome": "Thor",
                "tipo": "cao",
                "raca": "Golden Retriever",
                "idade": 3,
                "peso": 25.0,
                "sexo": "M",
                "castrado": true,
                "observacoes": "Pet calmo e dócil",
                "temAlergia": false,
                "precisaMedicacao": false,
                "comportamentoAgressivo": false,
                "clienteId": 1
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Thor"))
                .andExpect(jsonPath("$.tipo").value("cao"));

        verify(petService, times(1)).salvar(any(Pet.class), eq(1L));
    }

    @Test
    void testCriarPetGato() throws Exception {
        // Arrange
        Pet gato = new Pet();
        gato.setId(2L);
        gato.setNome("Mimi");
        gato.setTipo("gato");
        gato.setRaca("Persa");
        gato.setIdade(2);
        gato.setPeso(4.5);
        gato.setSexo("F");
        gato.setCastrado(true);
        gato.setCliente(cliente);

        when(petService.salvar(any(Pet.class), eq(1L))).thenReturn(gato);

        String petJson = """
            {
                "nome": "Mimi",
                "tipo": "gato",
                "raca": "Persa",
                "idade": 2,
                "peso": 4.5,
                "sexo": "F",
                "castrado": true,
                "clienteId": 1
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Mimi"))
                .andExpect(jsonPath("$.tipo").value("gato"))
                .andExpect(jsonPath("$.raca").value("Persa"));

        verify(petService, times(1)).salvar(any(Pet.class), eq(1L));
    }

    // ========== Testes de Atualização ==========

    @Test
    void testAtualizarPet() throws Exception {
        // Arrange
        pet.setNome("Thor Atualizado");
        pet.setIdade(4);
        when(petService.atualizar(eq(1L), any(Pet.class))).thenReturn(pet);

        String petJson = """
            {
                "nome": "Thor Atualizado",
                "tipo": "cao",
                "raca": "Golden Retriever",
                "idade": 4,
                "peso": 26.0,
                "sexo": "M",
                "castrado": true,
                "observacoes": "Pet calmo e dócil",
                "clienteId": 1
            }
            """;

        // Act & Assert
        mockMvc.perform(put("/api/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Thor Atualizado"))
                .andExpect(jsonPath("$.idade").value(4));

        verify(petService, times(1)).atualizar(eq(1L), any(Pet.class));
    }

    // ========== Testes de Deleção ==========

    @Test
    void testDeletarPet() throws Exception {
        // Arrange
        doNothing().when(petService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/pets/1"))
                .andExpect(status().isNoContent());

        verify(petService, times(1)).deletar(1L);
    }

    // ========== Testes de Necessidades Especiais ==========

    @Test
    void testPetComAlergia() throws Exception {
        // Arrange
        pet.setTemAlergia(true);
        pet.setObservacoes("Alérgico a frango");
        List<Pet> pets = Arrays.asList(pet);
        when(petService.listarTodos()).thenReturn(pets);

        // Act & Assert
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temAlergia").value(true))
                .andExpect(jsonPath("$[0].observacoes").value("Alérgico a frango"));

        verify(petService, times(1)).listarTodos();
    }

    @Test
    void testPetComMedicacao() throws Exception {
        // Arrange
        pet.setPrecisaMedicacao(true);
        List<Pet> pets = Arrays.asList(pet);
        when(petService.listarTodos()).thenReturn(pets);

        // Act & Assert
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precisaMedicacao").value(true));

        verify(petService, times(1)).listarTodos();
    }

    @Test
    void testPetComComportamentoAgressivo() throws Exception {
        // Arrange
        pet.setComportamentoAgressivo(true);
        pet.setObservacoes("Requer cuidado especial durante o banho");
        List<Pet> pets = Arrays.asList(pet);
        when(petService.listarTodos()).thenReturn(pets);

        // Act & Assert
        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comportamentoAgressivo").value(true));

        verify(petService, times(1)).listarTodos();
    }

    // ========== Testes de Diferentes Tipos de Pets ==========

    @Test
    void testBuscarPorTipoGato() throws Exception {
        // Arrange
        Pet gato = new Pet();
        gato.setId(2L);
        gato.setNome("Frajola");
        gato.setTipo("gato");
        gato.setRaca("Siamês");
        gato.setIdade(5);
        gato.setPeso(5.0);
        gato.setSexo("M");
        gato.setCastrado(false);
        gato.setCliente(cliente);

        List<Pet> gatos = Arrays.asList(gato);
        when(petService.buscarPorTipo("gato")).thenReturn(gatos);

        // Act & Assert
        mockMvc.perform(get("/api/pets/tipo/gato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("gato"))
                .andExpect(jsonPath("$[0].raca").value("Siamês"));

        verify(petService, times(1)).buscarPorTipo("gato");
    }

    @Test
    void testBuscarPorTipoPassaro() throws Exception {
        // Arrange
        Pet passaro = new Pet();
        passaro.setId(3L);
        passaro.setNome("Piu Piu");
        passaro.setTipo("passaro");
        passaro.setRaca("Canário");
        passaro.setIdade(1);
        passaro.setPeso(0.05);
        passaro.setSexo("M");
        passaro.setCastrado(false);
        passaro.setCliente(cliente);

        List<Pet> passaros = Arrays.asList(passaro);
        when(petService.buscarPorTipo("passaro")).thenReturn(passaros);

        // Act & Assert
        mockMvc.perform(get("/api/pets/tipo/passaro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("passaro"))
                .andExpect(jsonPath("$[0].nome").value("Piu Piu"));

        verify(petService, times(1)).buscarPorTipo("passaro");
    }
}
