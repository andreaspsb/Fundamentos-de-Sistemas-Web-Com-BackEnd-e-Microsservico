package com.petshop.functions.shared.repository;

import com.petshop.functions.shared.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByCategoriaId(Long categoriaId);

    List<Produto> findByAtivo(Boolean ativo);

    List<Produto> findByCategoriaIdAndAtivo(Long categoriaId, Boolean ativo);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque > 0")
    List<Produto> findProdutosDisponiveis();

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque > 0 AND p.categoria.id = :categoriaId")
    List<Produto> findProdutosDisponiveisPorCategoria(@Param("categoriaId") Long categoriaId);

    @Query("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) AND p.ativo = true")
    List<Produto> buscarPorNome(@Param("termo") String termo);

    List<Produto> findByQuantidadeEstoqueLessThan(Integer quantidade);
}
