package com.petshop.functions.shared.dto;

public class CategoriaResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private Boolean ativo;

    public CategoriaResponseDTO() {
    }

    public CategoriaResponseDTO(Long id, String nome, String descricao, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
