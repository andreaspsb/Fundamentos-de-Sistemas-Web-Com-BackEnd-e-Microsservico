package com.petshop.functions.shared.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data do agendamento é obrigatória")
    @Column(name = "data_agendamento", nullable = false)
    private LocalDate dataAgendamento;

    @NotNull(message = "Horário é obrigatório")
    @Column(nullable = false)
    private LocalTime horario;

    @NotBlank(message = "Método de atendimento é obrigatório")
    @Column(name = "metodo_atendimento", nullable = false, length = 20)
    private String metodoAtendimento; // telebusca, local

    @Column(name = "porte_pet", length = 20)
    private String portePet; // pequeno, medio, grande

    @Column(length = 500)
    private String observacoes;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToMany
    @JoinTable(
        name = "agendamento_servicos",
        joinColumns = @JoinColumn(name = "agendamento_id"),
        inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private List<Servico> servicos = new ArrayList<>();

    // Enum para status
    public enum StatusAgendamento {
        PENDENTE,
        CONFIRMADO,
        EM_ANDAMENTO,
        CONCLUIDO,
        CANCELADO
    }

    // Construtores
    public Agendamento() {
    }

    public Agendamento(LocalDate dataAgendamento, LocalTime horario, String metodoAtendimento, 
                       String portePet, String observacoes, Double valorTotal) {
        this.dataAgendamento = dataAgendamento;
        this.horario = horario;
        this.metodoAtendimento = metodoAtendimento;
        this.portePet = portePet;
        this.observacoes = observacoes;
        this.valorTotal = valorTotal;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDate dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public String getMetodoAtendimento() {
        return metodoAtendimento;
    }

    public void setMetodoAtendimento(String metodoAtendimento) {
        this.metodoAtendimento = metodoAtendimento;
    }

    public String getPortePet() {
        return portePet;
    }

    public void setPortePet(String portePet) {
        this.portePet = portePet;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    // Métodos auxiliares
    public void adicionarServico(Servico servico) {
        servicos.add(servico);
        servico.getAgendamentos().add(this);
    }

    public void removerServico(Servico servico) {
        servicos.remove(servico);
        servico.getAgendamentos().remove(this);
    }
}
