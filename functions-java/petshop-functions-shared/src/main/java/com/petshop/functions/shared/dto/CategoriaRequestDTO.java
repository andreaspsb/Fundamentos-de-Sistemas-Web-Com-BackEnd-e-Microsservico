package com.petshop.functions.shared.dto;

import jakarta.validation.constraints.*;

public class CategoriaRequestDTO {

    @NotBlank(message = "Nome da categoria é obrigatório")
    private String nome;

    private String descricao;

    private Boolean ativo = true;

    public CategoriaRequestDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
