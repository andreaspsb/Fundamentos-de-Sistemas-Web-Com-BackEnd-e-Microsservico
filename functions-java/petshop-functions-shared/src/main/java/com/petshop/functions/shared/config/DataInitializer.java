package com.petshop.functions.shared.config;

import com.petshop.functions.shared.model.*;
import com.petshop.functions.shared.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inicializador de dados para ambiente de desenvolvimento
 * Cria categorias, produtos, serviços e usuário admin
 */
@Configuration
@Profile({"dev", "development", "local"})
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository,
            ServicoRepository servicoRepository) {
        
        return args -> {
            logger.info("=== Iniciando DataInitializer ===");
            
            // Criar usuário admin
            initAdmin(usuarioRepository);
            
            // Criar categorias
            List<Categoria> categorias = initCategorias(categoriaRepository);
            
            // Criar produtos
            initProdutos(produtoRepository, categorias);
            
            // Criar serviços
            initServicos(servicoRepository);
            
            logger.info("=== DataInitializer concluído ===");
        };
    }

    private void initAdmin(UsuarioRepository usuarioRepository) {
        if (!usuarioRepository.existsByUsername("admin")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setSenha(encoder.encode("admin123"));
            admin.setEmail("admin@petshop.com");
            admin.setRole("Admin");
            admin.setAtivo(true);
            
            usuarioRepository.save(admin);
            logger.info("Usuário admin criado com sucesso");
        } else {
            logger.info("Usuário admin já existe");
        }
    }

    private List<Categoria> initCategorias(CategoriaRepository categoriaRepository) {
        if (categoriaRepository.count() == 0) {
            List<Categoria> categorias = List.of(
                createCategoria("Rações e Alimentação", "Rações premium, petiscos e suplementos alimentares para seu pet", true),
                createCategoria("Higiene e Cuidados", "Produtos de higiene, banho e cuidados especiais", true),
                createCategoria("Acessórios e Brinquedos", "Coleiras, guias, camas, brinquedos e acessórios diversos", true),
                createCategoria("Medicamentos", "Medicamentos e produtos de saúde veterinária", true),
                createCategoria("Casas e Tocas", "Casas, tocas e abrigos para pets", true)
            );
            
            List<Categoria> saved = categoriaRepository.saveAll(categorias);
            logger.info("{} categorias criadas", saved.size());
            return saved;
        } else {
            logger.info("Categorias já existem");
            return categoriaRepository.findAll();
        }
    }

    private Categoria createCategoria(String nome, String descricao, boolean ativo) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        categoria.setAtivo(ativo);
        return categoria;
    }

    private void initProdutos(ProdutoRepository produtoRepository, List<Categoria> categorias) {
        if (produtoRepository.count() == 0 && !categorias.isEmpty()) {
            Categoria racoes = categorias.stream().filter(c -> c.getNome().contains("Rações")).findFirst().orElse(categorias.get(0));
            Categoria higiene = categorias.stream().filter(c -> c.getNome().contains("Higiene")).findFirst().orElse(categorias.get(0));
            Categoria acessorios = categorias.stream().filter(c -> c.getNome().contains("Acessórios")).findFirst().orElse(categorias.get(0));

            List<Produto> produtos = List.of(
                // Rações
                createProduto("Ração Premium Cães Adultos 15kg", "Ração super premium para cães adultos de todas as raças", 
                    new BigDecimal("189.90"), 50, racoes, "racao-caes-premium.jpg", true),
                createProduto("Ração Premium Gatos Adultos 10kg", "Ração super premium para gatos adultos", 
                    new BigDecimal("159.90"), 40, racoes, "racao-gatos-premium.jpg", true),
                createProduto("Ração Filhotes Cães 8kg", "Ração especial para filhotes com DHA", 
                    new BigDecimal("129.90"), 35, racoes, "racao-filhotes.jpg", true),
                createProduto("Petisco Bifinho Carne 500g", "Petisco natural de carne bovina", 
                    new BigDecimal("24.90"), 100, racoes, "petisco-bifinho.jpg", true),
                createProduto("Ração Úmida Sachê Gatos 85g (12un)", "Pack com 12 sachês de ração úmida", 
                    new BigDecimal("45.90"), 60, racoes, "sache-gatos.jpg", true),
                    
                // Higiene
                createProduto("Shampoo Neutro para Cães 500ml", "Shampoo neutro dermatologicamente testado", 
                    new BigDecimal("32.90"), 80, higiene, "shampoo-neutro.jpg", true),
                createProduto("Condicionador Pelos Longos 500ml", "Condicionador para pets com pelos longos", 
                    new BigDecimal("34.90"), 70, higiene, "condicionador.jpg", true),
                createProduto("Spray Desembaraçador 250ml", "Spray para desembaraçar pelos", 
                    new BigDecimal("28.90"), 50, higiene, "spray-desembaracador.jpg", true),
                createProduto("Lenço Umedecido Pet (50un)", "Lenços umedecidos hipoalergênicos", 
                    new BigDecimal("19.90"), 120, higiene, "lenco-umedecido.jpg", true),
                createProduto("Escova Desembaraçadora", "Escova profissional para pelos", 
                    new BigDecimal("45.90"), 40, higiene, "escova.jpg", true),
                    
                // Acessórios
                createProduto("Coleira Ajustável Nylon M", "Coleira de nylon resistente tamanho M", 
                    new BigDecimal("29.90"), 90, acessorios, "coleira-nylon.jpg", true),
                createProduto("Guia Retrátil 5m", "Guia retrátil para passeios", 
                    new BigDecimal("89.90"), 45, acessorios, "guia-retratil.jpg", true),
                createProduto("Cama Pet Retangular G", "Cama confortável tamanho G", 
                    new BigDecimal("149.90"), 25, acessorios, "cama-pet.jpg", true),
                createProduto("Brinquedo Corda Dental", "Brinquedo de corda para limpeza dental", 
                    new BigDecimal("24.90"), 100, acessorios, "brinquedo-corda.jpg", true),
                createProduto("Bolinha Interativa com Som", "Bolinha que emite som ao morder", 
                    new BigDecimal("19.90"), 80, acessorios, "bolinha-som.jpg", true),
                createProduto("Comedouro Duplo Inox", "Comedouro e bebedouro em aço inox", 
                    new BigDecimal("59.90"), 55, acessorios, "comedouro-duplo.jpg", true)
            );

            produtoRepository.saveAll(produtos);
            logger.info("{} produtos criados", produtos.size());
        } else {
            logger.info("Produtos já existem ou categorias vazias");
        }
    }

    private Produto createProduto(String nome, String descricao, BigDecimal preco, int estoque, 
                                   Categoria categoria, String imagem, boolean disponivel) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco.doubleValue());
        produto.setQuantidadeEstoque(estoque);
        produto.setCategoria(categoria);
        produto.setUrlImagem(imagem);
        produto.setAtivo(disponivel);
        return produto;
    }

    private void initServicos(ServicoRepository servicoRepository) {
        if (servicoRepository.count() == 0) {
            List<Servico> servicos = List.of(
                createServico("Banho Pequeno Porte", "Banho completo para pets de pequeno porte (até 10kg)", 
                    new BigDecimal("50.00"), 60, true),
                createServico("Banho Médio Porte", "Banho completo para pets de médio porte (10-25kg)", 
                    new BigDecimal("70.00"), 90, true),
                createServico("Banho Grande Porte", "Banho completo para pets de grande porte (acima de 25kg)", 
                    new BigDecimal("90.00"), 120, true),
                createServico("Tosa Higiênica", "Tosa higiênica das partes íntimas e patas", 
                    new BigDecimal("35.00"), 30, true),
                createServico("Tosa Completa", "Tosa completa do corpo todo", 
                    new BigDecimal("80.00"), 90, true),
                createServico("Tosa na Máquina", "Tosa completa na máquina com acabamento", 
                    new BigDecimal("70.00"), 75, true),
                createServico("Banho + Tosa Higiênica", "Combo banho completo com tosa higiênica", 
                    new BigDecimal("75.00"), 90, true),
                createServico("Banho + Tosa Completa", "Combo banho completo com tosa completa", 
                    new BigDecimal("120.00"), 150, true),
                createServico("Corte de Unhas", "Corte profissional de unhas", 
                    new BigDecimal("25.00"), 15, true),
                createServico("Limpeza de Ouvidos", "Limpeza e higienização dos ouvidos", 
                    new BigDecimal("30.00"), 20, true),
                createServico("Hidratação de Pelos", "Tratamento de hidratação profunda", 
                    new BigDecimal("45.00"), 30, true),
                createServico("Consulta Veterinária", "Consulta com veterinário especializado", 
                    new BigDecimal("150.00"), 30, true),
                createServico("Vacinação", "Aplicação de vacinas (vacina não inclusa)", 
                    new BigDecimal("40.00"), 15, true),
                createServico("Hotel Pet (diária)", "Hospedagem com alimentação e cuidados", 
                    new BigDecimal("80.00"), 1440, true)
            );

            servicoRepository.saveAll(servicos);
            logger.info("{} serviços criados", servicos.size());
        } else {
            logger.info("Serviços já existem");
        }
    }

    private Servico createServico(String nome, String descricao, BigDecimal preco, int duracao, boolean ativo) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricao(descricao);
        servico.setPreco(preco.doubleValue());
        // duracao ignorado - Servico não tem campo duracaoMinutos
        servico.setAtivo(ativo);
        return servico;
    }
}
