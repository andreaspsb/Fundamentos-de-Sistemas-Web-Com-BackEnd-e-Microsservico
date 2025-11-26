package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.model.Categoria;
import com.petshop.service.CategoriaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CategoriaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.petshop.security.JwtAuthenticationFilter.class))
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    @Test
    void deveListarTodasCategorias() throws Exception {
        Categoria cat1 = new Categoria();
        cat1.setId(1L);
        cat1.setNome("Rações");
        cat1.setAtivo(true);
        
        Categoria cat2 = new Categoria();
        cat2.setId(2L);
        cat2.setNome("Higiene");
        cat2.setAtivo(true);
        
        List<Categoria> categorias = Arrays.asList(cat1, cat2);
        
        when(categoriaService.listarTodas()).thenReturn(categorias);
        
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Rações"));
    }

    @Test
    void deveBuscarCategoriaPorId() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Rações");
        categoria.setAtivo(true);
        
        when(categoriaService.buscarPorId(1L)).thenReturn(Optional.of(categoria));
        
        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rações"));
    }

    @Test
    void deveCriarCategoria() throws Exception {
        Categoria request = new Categoria();
        request.setNome("Nova Categoria");
        request.setDescricao("Descrição");
        request.setAtivo(true);
        
        Categoria response = new Categoria();
        response.setId(1L);
        response.setNome("Nova Categoria");
        response.setAtivo(true);
        
        when(categoriaService.salvar(any(Categoria.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Nova Categoria"));
    }

    @Test
    void deveAtualizarCategoria() throws Exception {
        Categoria request = new Categoria();
        request.setNome("Categoria Atualizada");
        request.setAtivo(true);
        
        Categoria response = new Categoria();
        response.setId(1L);
        response.setNome("Categoria Atualizada");
        response.setAtivo(true);
        
        when(categoriaService.atualizar(eq(1L), any(Categoria.class))).thenReturn(response);
        
        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Categoria Atualizada"));
    }

    @Test
    void deveDeletarCategoria() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }
}
