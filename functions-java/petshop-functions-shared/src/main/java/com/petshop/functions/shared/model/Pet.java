package com.petshop.functions.shared.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do pet é obrigatório")
    @Size(min = 2, message = "Nome do pet deve ter no mínimo 2 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Tipo de pet é obrigatório")
    @Column(nullable = false, length = 20)
    private String tipo; // cao, gato, passaro, coelho, outro

    @NotBlank(message = "Raça é obrigatória")
    @Column(nullable = false)
    private String raca;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 30, message = "Idade não pode ser maior que 30 anos")
    @Column(nullable = false)
    private Integer idade;

    @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero")
    @DecimalMax(value = "100.0", message = "Peso não pode ser maior que 100kg")
    private Double peso;

    @NotBlank(message = "Sexo do pet é obrigatório")
    @Column(nullable = false, length = 1)
    private String sexo; // M, F

    @Column(nullable = false)
    private Boolean castrado = false;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "tem_alergia")
    private Boolean temAlergia = false;

    @Column(name = "precisa_medicacao")
    private Boolean precisaMedicacao = false;

    @Column(name = "comportamento_agressivo")
    private Boolean comportamentoAgressivo = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();

    // Construtores
    public Pet() {
    }

    public Pet(String nome, String tipo, String raca, Integer idade, Double peso, String sexo, 
               Boolean castrado, String observacoes) {
        this.nome = nome;
        this.tipo = tipo;
        this.raca = raca;
        this.idade = idade;
        this.peso = peso;
        this.sexo = sexo;
        this.castrado = castrado;
        this.observacoes = observacoes;
    }

    // Getters e Setters
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }
}
