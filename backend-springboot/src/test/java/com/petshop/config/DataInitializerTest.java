package com.petshop.config;

import com.petshop.repository.CategoriaRepository;
import com.petshop.repository.ProdutoRepository;
import com.petshop.repository.ServicoRepository;
import com.petshop.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Testes para o inicializador de dados.
 * Anotação SuppressWarnings("null") usada para suprimir warnings do Mockito any().
 */
@SuppressWarnings("null")
class DataInitializerTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void naoDeveCarregarDadosSeJaExistirem() throws Exception {
        when(categoriaRepository.count()).thenReturn(1L);

        dataInitializer.run();

        verify(categoriaRepository).count();
        verify(categoriaRepository, never()).save(any());
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveCarregarDadosIniciaisQuandoVazio() throws Exception {
        when(categoriaRepository.count()).thenReturn(0L);
        when(categoriaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        dataInitializer.run();

        verify(categoriaRepository).count();
        verify(categoriaRepository, atLeast(3)).save(any());
        verify(produtoRepository, atLeast(6)).save(any());
        verify(servicoRepository, atLeast(3)).save(any());
        verify(usuarioRepository, atLeast(1)).save(any());
    }
}
