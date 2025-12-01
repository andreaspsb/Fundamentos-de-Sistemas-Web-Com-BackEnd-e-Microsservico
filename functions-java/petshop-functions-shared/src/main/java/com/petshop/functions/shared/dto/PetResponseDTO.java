package com.petshop.functions.shared.dto;

public class PetResponseDTO {

    private Long id;
    private String nome;
    private String tipo;
    private String raca;
    private Integer idade;
    private Double peso;
    private String sexo;
    private Boolean castrado;
    private String observacoes;
    private Boolean temAlergia;
    private Boolean precisaMedicacao;
    private Boolean comportamentoAgressivo;
    private Long clienteId;
    private String clienteNome;

    public PetResponseDTO() {
    }

    public PetResponseDTO(Long id, String nome, String tipo, String raca, Integer idade, Double peso, 
                         String sexo, Boolean castrado, String observacoes, Boolean temAlergia, 
                         Boolean precisaMedicacao, Boolean comportamentoAgressivo, 
                         Long clienteId, String clienteNome) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.raca = raca;
        this.idade = idade;
        this.peso = peso;
        this.sexo = sexo;
        this.castrado = castrado;
        this.observacoes = observacoes;
        this.temAlergia = temAlergia;
        this.precisaMedicacao = precisaMedicacao;
        this.comportamentoAgressivo = comportamentoAgressivo;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
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

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }
}
