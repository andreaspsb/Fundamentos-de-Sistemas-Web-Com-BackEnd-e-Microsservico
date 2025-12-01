package com.petshop.functions.shared.dto;

import jakarta.validation.constraints.*;

public class PetRequestDTO {

    @NotBlank(message = "Nome do pet é obrigatório")
    @Size(min = 2, message = "Nome do pet deve ter no mínimo 2 caracteres")
    private String nome;

    @NotBlank(message = "Tipo de pet é obrigatório")
    private String tipo;

    @NotBlank(message = "Raça é obrigatória")
    private String raca;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 30, message = "Idade não pode ser maior que 30 anos")
    private Integer idade;

    @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero")
    @DecimalMax(value = "100.0", message = "Peso não pode ser maior que 100kg")
    private Double peso;

    @NotBlank(message = "Sexo do pet é obrigatório")
    private String sexo;

    private Boolean castrado = false;

    private String observacoes;

    private Boolean temAlergia = false;

    private Boolean precisaMedicacao = false;

    private Boolean comportamentoAgressivo = false;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    public PetRequestDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Boolean getCastrado() {
        return castrado;
    }

    public void setCastrado(Boolean castrado) {
        this.castrado = castrado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Boolean getTemAlergia() {
        return temAlergia;
    }

    public void setTemAlergia(Boolean temAlergia) {
        this.temAlergia = temAlergia;
    }

    public Boolean getPrecisaMedicacao() {
        return precisaMedicacao;
    }

    public void setPrecisaMedicacao(Boolean precisaMedicacao) {
        this.precisaMedicacao = precisaMedicacao;
    }

    public Boolean getComportamentoAgressivo() {
        return comportamentoAgressivo;
    }

    public void setComportamentoAgressivo(Boolean comportamentoAgressivo) {
        this.comportamentoAgressivo = comportamentoAgressivo;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
