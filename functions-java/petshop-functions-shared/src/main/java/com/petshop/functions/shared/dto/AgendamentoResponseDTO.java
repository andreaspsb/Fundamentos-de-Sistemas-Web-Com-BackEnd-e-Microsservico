package com.petshop.functions.shared.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AgendamentoResponseDTO {

    private Long id;
    private LocalDate dataAgendamento;
    private LocalTime horario;
    private String metodoAtendimento;
    private String portePet;
    private String observacoes;
    private Double valorTotal;
    private String status;
    private Long clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private Long petId;
    private String petNome;
    private List<ServicoSimpleDTO> servicos;

    public AgendamentoResponseDTO() {
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetNome() {
        return petNome;
    }

    public void setPetNome(String petNome) {
        this.petNome = petNome;
    }

    public List<ServicoSimpleDTO> getServicos() {
        return servicos;
    }

    public void setServicos(List<ServicoSimpleDTO> servicos) {
        this.servicos = servicos;
    }
}
